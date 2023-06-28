/**************************************************************************
 * Copyright (c) 2023 Dmytro Ostapenko. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **************************************************************************/


package org.teslasoft.assistant.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.provider.DocumentsContract
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast

import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.audio.TranscriptionRequest
import com.aallam.openai.api.chat.ChatCompletionChunk
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.completion.CompletionRequest
import com.aallam.openai.api.completion.TextCompletion
import com.aallam.openai.api.file.FileSource
import com.aallam.openai.api.image.ImageCreation
import com.aallam.openai.api.image.ImageSize
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.api.model.Model
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentifier

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import okio.FileSystem
import okio.Path.Companion.toPath

import org.jetbrains.annotations.TestOnly
import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.ChatPreferences
import org.teslasoft.assistant.preferences.Preferences
import org.teslasoft.assistant.ui.adapters.ChatAdapter
import org.teslasoft.assistant.ui.onboarding.WelcomeActivity
import org.teslasoft.assistant.ui.permission.MicrophonePermissionActivity
import org.teslasoft.assistant.util.Hash
import org.teslasoft.assistant.util.LocaleParser

import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

import java.net.URL

import java.util.Base64
import java.util.Locale

class ChatActivity : FragmentActivity() {

    // Init UI
    private var messageInput: EditText? = null
    private var btnSend: ImageButton? = null
    private var btnMicro: ImageButton? = null
    private var btnSettings: ImageButton? = null
    private var progress: ProgressBar? = null
    private var chat: ListView? = null
    private var activityTitle: TextView? = null
    private var btnExport: ImageButton? = null
    private var fileContents: ByteArray? = null

    // Init chat
    private var messages: ArrayList<HashMap<String, Any>> = arrayListOf()
    private var adapter: ChatAdapter? = null
    @OptIn(BetaOpenAI::class)
    private var chatMessages: ArrayList<ChatMessage> = arrayListOf()
    private var chatId = ""
    private var chatName = ""
    private lateinit var languageIdentifier: LanguageIdentifier

    // Init states
    private var isRecording = false
    private var keyboardMode = false
    private var isTTSInitialized = false
    private var silenceMode = false
    private var autoLangDetect = false

    // init AI
    private var ai: OpenAI? = null
    private var key: String? = null
    private var model = ""
    private var endSeparator = ""
    private var prefix = ""

    // Init DALL-e
    private var resolution = "512x152"

    // Init audio
    private var recognizer: SpeechRecognizer? = null
    private var recorder: MediaRecorder? = null

    @OptIn(BetaOpenAI::class)
    private val speechListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) { /* unused */ }
        override fun onBeginningOfSpeech() { /* unused */ }
        override fun onRmsChanged(rmsdB: Float) { /* unused */ }
        override fun onBufferReceived(buffer: ByteArray?) { /* unused */ }
        override fun onPartialResults(partialResults: Bundle?) { /* unused */ }
        override fun onEvent(eventType: Int, params: Bundle?) { /* unused */ }

        override fun onEndOfSpeech() {
            isRecording = false
            btnMicro?.setImageResource(R.drawable.ic_microphone)
        }

        override fun onError(error: Int) {
            isRecording = false
            btnMicro?.setImageResource(R.drawable.ic_microphone)
        }

        override fun onResults(results: Bundle?) {
            isRecording = false
            btnMicro?.setImageResource(R.drawable.ic_microphone)
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (matches != null && matches.size > 0) {
                val recognizedText = matches[0]

                putMessage(prefix + recognizedText + endSeparator, false)

                chatMessages.add(ChatMessage(
                    role = ChatRole.User,
                    content = prefix + recognizedText + endSeparator
                ))

                saveSettings()

                btnMicro?.isEnabled = false
                btnSend?.isEnabled = false
                progress?.visibility = View.VISIBLE

                CoroutineScope(Dispatchers.Main).launch {
                    generateResponse(prefix + recognizedText + endSeparator, true)
                }
            }
        }
    }

    // Init TTS
    private var tts: TextToSpeech? = null
    private val ttsListener: TextToSpeech.OnInitListener =
        TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsPostInit()
            }
        }

    private fun ttsPostInit() {
        if (!autoLangDetect) {
            val result = tts!!.setLanguage(
                LocaleParser.parse(
                    Preferences.getPreferences(
                        this@ChatActivity,
                        chatId
                    ).getLanguage()
                )
            )

            isTTSInitialized =
                !(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)

            val voices: Set<Voice> = tts!!.voices
            for (v: Voice in voices) {
                if (v.name.equals("en-us-x-iom-local") && Preferences.getPreferences(
                        this@ChatActivity,
                        chatId
                    ).getLanguage() == "en"
                ) {
                    tts!!.voice = v
                }
            }

            /*
            * Voice models (english: en-us-x):
            * sfg-local
            * iob-network
            * iom-local
            * iog-network
            * tpc-local
            * tpf-local
            * sfg-network
            * iob-local
            * tpd-network
            * tpc-network
            * iol-network
            * iom-network
            * tpd-local
            * tpf-network
            * iog-local
            * iol-local
            * */
        }
    }

    // Init permissions screen
    private val permissionResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        run {
            if (result.resultCode == Activity.RESULT_OK) {
                startRecognition()
            }
        }
    }

    private val permissionResultLauncherV2 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        run {
            if (result.resultCode == Activity.RESULT_OK) {
                startWhisper()
            }
        }
    }

    private val settingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { recreate() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_chat)
        languageIdentifier = LanguageIdentification.getClient()


        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        window.statusBarColor = ContextCompat.getColor(this, R.color.accent_100)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.accent_100)

        initChatId()

        initSettings()
    }

    public override fun onDestroy() {
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }

    /** SYSTEM INITIALIZATION START **/

    @OptIn(BetaOpenAI::class)
    @Suppress("unchecked")
    private fun initSettings() {
        key = Preferences.getPreferences(this, chatId).getApiKey(this)

        endSeparator = Preferences.getPreferences(this, chatId).getEndSeparator()
        prefix = Preferences.getPreferences(this, chatId).getPrefix()

        loadResolution()

        if (key == null) {
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        } else {
            silenceMode = Preferences.getPreferences(this, chatId).getSilence()
            autoLangDetect = Preferences.getPreferences(this, chatId).getAutoLangDetect()
            messages = ChatPreferences.getChatPreferences().getChatById(this, chatId)

            if (messages == null) messages = arrayListOf()

            if (chatMessages == null) chatMessages = arrayListOf()

            for (message: HashMap<String, Any> in messages) {
                if (!message["message"].toString().contains("data:image")) {
                    if (message["isBot"] == true) {
                        chatMessages.add(
                            ChatMessage(
                                role = ChatRole.Assistant,
                                content = message["message"].toString()
                            )
                        )
                    } else {
                        chatMessages.add(
                            ChatMessage(
                                role = ChatRole.User,
                                content = message["message"].toString()
                            )
                        )
                    }
                }
            }

            adapter = ChatAdapter(messages, this, chatId)

            initUI()
            initSpeechListener()
            initTTS()
            initLogic()
            initAI()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initUI() {
        btnMicro = findViewById(R.id.btn_micro)
        btnSettings = findViewById(R.id.btn_settings)
        chat = findViewById(R.id.messages)
        messageInput = findViewById(R.id.message_input)
        btnSend = findViewById(R.id.btn_send)
        progress = findViewById(R.id.progress)
        activityTitle = findViewById(R.id.chat_activity_title)
        btnExport = findViewById(R.id.btn_export)

        btnExport?.setImageResource(R.drawable.ic_upload)

        activityTitle?.text = chatName

        progress?.visibility = View.GONE

        btnMicro?.setImageResource(R.drawable.ic_microphone)
        btnSettings?.setImageResource(R.drawable.ic_settings)

        chat?.adapter = adapter
        chat?.dividerHeight = 0

        adapter?.notifyDataSetChanged()
    }

    private fun initLogic() {
        btnMicro?.setOnClickListener {
            if (Preferences.getPreferences(this, chatId).getAudioModel() == "google") {
                handleGoogleSpeechRecognition()
            } else {
                handleWhisperSpeechRecognition()
            }
        }

        messageInput?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                /* unused */
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString() == "") {
                    btnSend?.visibility = View.GONE
                    btnMicro?.visibility = View.VISIBLE
                } else {
                    btnSend?.visibility = View.VISIBLE
                    btnMicro?.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {
                /* unused */
            }

        })

        btnSend?.setOnClickListener {
            parseMessage(messageInput?.text.toString())
        }

        btnSettings?.setOnClickListener {
            val i = Intent(
                    this,
                    SettingsActivity::class.java
            )

            i.putExtra("chatId", chatId)

            settingsLauncher.launch(
                i
            )
        }

        btnExport?.setOnClickListener {
            val gson = Gson()
            val json: String = gson.toJson(messages)

            fileContents = json.toByteArray()

            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
                putExtra(Intent.EXTRA_TITLE, "$chatId.json")
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse("/storage/emulated/0/SpeakGPT/$chatId.json"))
            }
            fileSaveIntentLauncher.launch(intent)
        }
    }

    private val fileSaveIntentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        run {
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.also { uri ->
                    writeToFile(uri)
                }
            }
        }
    }

    private fun writeToFile(uri: Uri) {
        try {
            contentResolver.openFileDescriptor(uri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use {
                    it.write(
                        fileContents
                    )
                }
            }
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
        } catch (e: FileNotFoundException) {
            Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        } catch (e: IOException) {
            Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun startWhisper() {
        if (android.os.Build.VERSION.SDK_INT >= 31) {
            recorder = MediaRecorder(this).apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC)
                setAudioChannels(1)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(96000)
                setOutputFile("${externalCacheDir?.absolutePath}/tmp.m4a")

                try {
                    prepare()
                } catch (e: IOException) {
                    btnMicro?.setImageResource(R.drawable.ic_microphone)
                    isRecording = false
                    MaterialAlertDialogBuilder(this@ChatActivity, R.style.App_MaterialAlertDialog)
                        .setTitle("Audio error")
                        .setMessage("Failed to initialize microphone")
                        .setPositiveButton("Close") { _, _, -> }
                        .show()
                }

                start()
            }
        } else {
            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC)
                setAudioChannels(1)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(96000)
                setOutputFile("${externalCacheDir?.absolutePath}/tmp.m4a")

                try {
                    prepare()
                } catch (e: IOException) {
                    btnMicro?.setImageResource(R.drawable.ic_microphone)
                    isRecording = false
                    MaterialAlertDialogBuilder(this@ChatActivity, R.style.App_MaterialAlertDialog)
                        .setTitle("Audio error")
                        .setMessage("Failed to initialize microphone")
                        .setPositiveButton("Close") { _, _, -> }
                        .show()
                }

                start()
            }
        }
    }

    private fun stopWhisper() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null

        btnMicro?.isEnabled = false
        btnSend?.isEnabled = false
        progress?.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.Main).launch {
            processRecording()
        }
    }

    @OptIn(BetaOpenAI::class)
    private suspend fun processRecording() {
        try {
            val transcriptionRequest = TranscriptionRequest(
                audio = FileSource(
                    path = "${externalCacheDir?.absolutePath}/tmp.m4a".toPath(),
                    fileSystem = FileSystem.SYSTEM
                ),
                model = ModelId("whisper-1"),
            )
            val transcription = ai?.transcription(transcriptionRequest)!!.text

            putMessage(prefix + transcription + endSeparator, false)

            chatMessages.add(
                ChatMessage(
                    role = ChatRole.User,
                    content = prefix + transcription + endSeparator
                )
            )

            saveSettings()

            btnMicro?.isEnabled = false
            btnSend?.isEnabled = false
            progress?.visibility = View.VISIBLE

            CoroutineScope(Dispatchers.Main).launch {
                generateResponse(prefix + transcription + endSeparator, true)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to record audio", Toast.LENGTH_SHORT).show()
            btnMicro?.isEnabled = true
            btnSend?.isEnabled = true
            progress?.visibility = View.GONE
        }
    }

    private fun handleWhisperSpeechRecognition() {
        if (isRecording) {
            btnMicro?.setImageResource(R.drawable.ic_microphone)
            isRecording = false
            stopWhisper()
        } else {
            btnMicro?.setImageResource(R.drawable.ic_stop_recording)
            isRecording = true

            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startWhisper()
            } else {
                permissionResultLauncherV2.launch(
                    Intent(
                        this,
                        MicrophonePermissionActivity::class.java
                    )
                )
            }
        }
    }

    private fun handleGoogleSpeechRecognition() {
        if (isRecording) {
            tts!!.stop()
            btnMicro?.setImageResource(R.drawable.ic_microphone)
            recognizer?.stopListening()
            isRecording = false
        } else {
            tts!!.stop()
            btnMicro?.setImageResource(R.drawable.ic_stop_recording)
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startRecognition()
            } else {
                permissionResultLauncher.launch(
                    Intent(
                        this,
                        MicrophonePermissionActivity::class.java
                    )
                )
            }

            isRecording = true
        }
    }

    private fun initSpeechListener() {
        recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recognizer?.setRecognitionListener(speechListener)
    }

    private fun initTTS() {
        tts = TextToSpeech(this, ttsListener)
    }

    private fun initAI() {
        if (key == null) {
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        } else {
            val config = OpenAIConfig(
                token = key!!,
                logLevel = LogLevel.None,
            )
            ai = OpenAI(config)
            loadModel()
            setup()
        }
    }

    private fun initChatId() {
        val extras: Bundle? = intent.extras

        if (extras != null) {
            chatId = extras.getString("chatId", "")

            chatName = extras.getString("name", "")
        }
    }

    /*
    * Setup SpeakGPT with activation prompt.
    * */
    @OptIn(BetaOpenAI::class)
    private fun setup() {
        if (messages.isEmpty()) {
            val prompt: String = Preferences.getPreferences(this, chatId).getPrompt()

            if (prompt.toString() != "" && prompt.toString() != "null" && prompt != "") {
                putMessage(prompt, false)

                chatMessages.add(
                    ChatMessage(
                        role = ChatRole.User,
                        content = prompt
                    )
                )

                btnMicro?.isEnabled = false
                btnSend?.isEnabled = false
                progress?.visibility = View.VISIBLE

                CoroutineScope(Dispatchers.Main).launch {
                    generateResponse(prompt, false)
                }
            }
        }
    }

    private fun loadModel() {
        model = Preferences.getPreferences(this, chatId).getModel()
        endSeparator = Preferences.getPreferences(this, chatId).getEndSeparator()
        prefix = Preferences.getPreferences(this, chatId).getPrefix()
    }

    @TestOnly
    private suspend fun getModels() {
        val models: List<Model> = ai!!.models()

        var string = "";

        for (m: Model in models) {
            val tmp: String = m.id.toString().replace(")", "").replace("ModelId(id=", "")

            if (tmp.contains("gpt")) string += "$tmp\n"
        }

        MaterialAlertDialogBuilder(this, R.style.App_MaterialAlertDialog)
            .setTitle("Debug")
            .setMessage(string)
            .setPositiveButton("Close") { _, _, -> }
            .show()
    }

    // Init image resolutions
    private fun loadResolution() {
        resolution = Preferences.getPreferences(this, chatId).getResolution()
    }

    /** SYSTEM INITIALIZATION END **/

    private fun saveSettings() {
        val chat = getSharedPreferences("chat_$chatId", MODE_PRIVATE)
        val editor = chat.edit()
        val gson = Gson()
        val json: String = gson.toJson(messages)

        editor.putString("chat", json)
        editor.apply()
    }

    @OptIn(BetaOpenAI::class)
    private fun parseMessage(message: String) {
        tts!!.stop()
        if (message != "") {
            messageInput?.setText("")

            keyboardMode = false

            val m = prefix + message + endSeparator

            putMessage(m, false)
            saveSettings()

            btnMicro?.isEnabled = false
            btnSend?.isEnabled = false
            progress?.visibility = View.VISIBLE

            if (m.lowercase().contains("/imagine") && m.length > 9) {
                val x: String = m.substring(9)

                sendImageRequest(x)
            } else if (m.lowercase().contains("/imagine") && m.length <= 9) {
                putMessage("Prompt can not be empty. Use /imagine &lt;PROMPT&gt;", true)

                saveSettings()

                btnMicro?.isEnabled = true
                btnSend?.isEnabled = true
                progress?.visibility = View.GONE
            } else if (m.lowercase().contains("create an image") ||
                m.lowercase().contains("generate an image") ||
                m.lowercase().contains("create image") ||
                m.lowercase().contains("generate image") ||
                m.lowercase().contains("create a photo") ||
                m.lowercase().contains("generate a photo") ||
                m.lowercase().contains("create photo") ||
                m.lowercase().contains("generate photo")) {
                sendImageRequest(m)
            } else {
                chatMessages.add(ChatMessage(
                    role = ChatRole.User,
                    content = m
                ))

                CoroutineScope(Dispatchers.Main).launch {
                    generateResponse(m, false)
                }
            }
        }
    }

    private fun sendImageRequest(str: String) {
        CoroutineScope(Dispatchers.Main).launch {
            generateImage(str)
        }
    }

    private fun startRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, LocaleParser.parse(Preferences.getPreferences(this@ChatActivity, chatId).getLanguage()))
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)

        recognizer?.startListening(intent)
    }

    private fun putMessage(message: String, isBot: Boolean) {
        val map: HashMap<String, Any> = HashMap()

        map["message"] = message
        map["isBot"] = isBot

        messages.add(map)
        adapter?.notifyDataSetChanged()

        chat?.post {
            chat?.setSelection(adapter?.count!! - 1)
        }
    }

    @OptIn(BetaOpenAI::class)
    private suspend fun generateResponse(request: String, shouldPronounce: Boolean) {
        putMessage("", true)
        var response = ""

        try {
            if (model.contains("davinci") || model.contains("curie") || model.contains("babbage") || model.contains("ada") || model.contains(":ft-")) {

                // val tokens = Preferences.getPreferences(this, chatId).getMaxTokens()

                val completionRequest = CompletionRequest(
                    model = ModelId(model),
                    prompt = request,
                    echo = false
                )

                val completions: Flow<TextCompletion> = ai!!.completions(completionRequest)

                completions.collect { v ->
                    run {
                        if (v.choices[0].text != "null") {
                            response += v.choices[0].text
                            messages[messages.size - 1]["message"] = "$response █"
                            adapter?.notifyDataSetChanged()
                        }
                    }
                }
            } else {
                // val tokens = Preferences.getPreferences(this, chatId).getMaxTokens()

                val chatCompletionRequest = ChatCompletionRequest(
                    model = ModelId(model),
                    messages = chatMessages
                )

                val completions: Flow<ChatCompletionChunk> = ai!!.chatCompletions(chatCompletionRequest)

                completions.collect { v ->
                    run {
                        if (v.choices[0].delta!!.content != null) {
                            response += v.choices[0].delta?.content
                            messages[messages.size - 1]["message"] = "$response █"
                            adapter?.notifyDataSetChanged()
                        }
                    }
                }
            }

            messages[messages.size - 1]["message"] = "$response\n"
            adapter?.notifyDataSetChanged()

            chatMessages.add(ChatMessage(
                role = ChatRole.Assistant,
                content = response
            ))

            if (shouldPronounce && isTTSInitialized && !silenceMode) {
                if (autoLangDetect) {
                    languageIdentifier.identifyLanguage(response)
                        .addOnSuccessListener { languageCode ->
                            if (languageCode == "und") {
                                Log.i("MLKit", "Can't identify language.")
                            } else {
                                Log.i("MLKit", "Language: $languageCode")
                                tts!!.language = Locale.forLanguageTag(
                                    languageCode
                                )
                            }

                            tts!!.speak(response, TextToSpeech.QUEUE_FLUSH, null, "")
                        }.addOnFailureListener {
                            // Ignore auto language detection if an error is occurred
                            autoLangDetect = false
                            ttsPostInit()

                            tts!!.speak(response, TextToSpeech.QUEUE_FLUSH, null, "")
                        }
                } else {
                    tts!!.speak(response, TextToSpeech.QUEUE_FLUSH, null, "")
                }
            }
        } catch (e: Exception) {
            response += if (e.stackTraceToString().contains("does not exist")) {
                "Looks like this model (${model}) is not available to you right now. It can be because of high demand or this model is currently in limited beta."
            } else if (e.stackTraceToString().contains("Connect timeout has expired") || e.stackTraceToString().contains("SocketTimeoutException")) {
                "Could not connect to OpenAI servers. It may happen when your Internet speed is slow or too many users are using this model at the same time. Try to switch to another model."
            } else if (e.stackTraceToString().contains("This model's maximum")) {
                "Too many tokens. It is an internal error, please report it. Also try to truncate your input. Sometimes it may help."
            } else if (e.stackTraceToString().contains("No address associated with hostname")) {
                "You are currently offline. Please check your connection and try again."
            } else if (e.stackTraceToString().contains("Incorrect API key")) {
                "Your API key is incorrect. Change it in Settings > Change OpenAI key. If you think this is an error please check if your API key has not been rotated. If you accidentally published your key it might be automatically revoked."
            } else if (e.stackTraceToString().contains("you must provide a model")) {
                "No valid model is set in settings. Please change the model and try again."
            } else if (e.stackTraceToString().contains("Software caused connection abort")) {
                "\n\n[error] An error occurred while generating response. It may be due to a weak connection or high demand. Try to switch to another model or try again later."
            } else if (e.stackTraceToString().contains("You exceeded your current quota")) {
                "You exceeded your current quota. If you had free trial usage please add payment info. Also please check your usage limits. You can change your limits in Account settings."
            } else {
                e.stackTraceToString()
            }

            messages[messages.size - 1]["message"] = "${response}\n"
            adapter?.notifyDataSetChanged()
        }

        saveSettings()

        btnMicro?.isEnabled = true
        btnSend?.isEnabled = true
        progress?.visibility = View.GONE
    }

    private fun writeImageToCache(bytes: ByteArray) {
        try {
            contentResolver.openFileDescriptor(Uri.fromFile(File(getExternalFilesDir("images")?.absolutePath + "/" + Hash.hash(Base64.getEncoder().encodeToString(bytes)) + ".png")), "w")?.use {
                FileOutputStream(it.fileDescriptor).use {
                    it.write(
                        bytes
                    )
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @OptIn(BetaOpenAI::class)
    private suspend fun generateImage(p: String) {
        try {
            val images = ai?.imageURL(
                creation = ImageCreation(
                    prompt = p,
                    n = 1,
                    size = ImageSize(resolution)
                )
            )

            val url = URL(images?.get(0)?.url!!)

            val `is` = withContext(Dispatchers.IO) {
                url.openStream()
            }
            val bytes: ByteArray = org.apache.commons.io.IOUtils.toByteArray(`is`)

            writeImageToCache(bytes)

            val encoded = Base64.getEncoder().encodeToString(bytes)

            val file = Hash.hash(encoded)
            putMessage("~file:$file", true)
        } catch (e: Exception) {
            if (e.stackTraceToString().contains("Your request was rejected")) {
                putMessage("Your prompt contains inappropriate content and can not be processed. We strive to make AI safe and relevant for everyone.", true)
            } else if (e.stackTraceToString().contains("No address associated with hostname")) {
                putMessage("You are currently offline. Please check your connection and try again.", true);
            } else if (e.stackTraceToString().contains("Incorrect API key")) {
                putMessage("Your API key is incorrect. Change it in Settings > Change OpenAI key. If you think this is an error please check if your API key has not been rotated. If you accidentally published your key it might be automatically revoked.", true);
            } else if (e.stackTraceToString().contains("Software caused connection abort")) {
                putMessage("An error occurred while generating response. It may be due to a weak connection or high demand. Try again later.", true);
            } else if (e.stackTraceToString().contains("You exceeded your current quota")) {
                putMessage("You exceeded your current quota. If you had free trial usage please add payment info. Also please check your usage limits. You can change your limits in Account settings.", true)
            } else {
                putMessage(e.stackTraceToString(), true)
            }
        }

        saveSettings()

        btnMicro?.isEnabled = true
        btnSend?.isEnabled = true
        progress?.visibility = View.GONE
    }
}
