package org.teslasoft.assistant

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.StrictMode
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ChatCompletionChunk
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.completion.CompletionRequest
import com.aallam.openai.api.completion.TextCompletion
import com.aallam.openai.api.image.ImageCreation
import com.aallam.openai.api.image.ImageSize
import com.aallam.openai.api.model.Model
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.ktor.util.reflect.Type
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.teslasoft.assistant.adapters.ChatAdapter
import org.teslasoft.assistant.onboarding.WelcomeActivity
import org.teslasoft.assistant.settings.SettingsActivity
import org.teslasoft.assistant.ui.MicrophonePermissionScreen
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

    // Init chat
    private var messages: ArrayList<HashMap<String, Any>> = arrayListOf()
    private var adapter: ChatAdapter? = null
    @OptIn(BetaOpenAI::class)
    private var chatMessages: ArrayList<ChatMessage> = arrayListOf()
    private var chatId = ""
    private var chatName = ""

    // Init states
    private var isRecording = false
    private var keyboardMode = false
    private var isTTSInitialized = false
    private var silenceMode = false

    // init AI
    private var ai: OpenAI? = null
    private var key: String? = null
    private var model = ""

    // Init DALL-e
    private var resolution = "512x152"

    // Init audio
    private var recognizer: SpeechRecognizer? = null
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

                putMessage(recognizedText, false)
                saveSettings()

                btnMicro?.isEnabled = false
                btnSend?.isEnabled = false
                progress?.visibility = View.VISIBLE

                CoroutineScope(Dispatchers.Main).launch {
                    generateResponse(recognizedText, true)
                }
            }
        }
    }

    // Init TTS
    private var tts: TextToSpeech? = null
    private val ttsListener: TextToSpeech.OnInitListener =
        TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts!!.setLanguage(Locale.US)

                isTTSInitialized = !(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)

                val voices: Set<Voice> = tts!!.voices
                for (v: Voice in voices) {
                    if (v.name.equals("en-us-x-iom-local")) {
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

    private val settingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { recreate() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_chat)

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
        val settings: SharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)

        key = settings.getString("api_key", null)

        loadResolution()

        if (key == null) {
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        } else {
            val silenceSettings: SharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)
            silenceMode = silenceSettings.getBoolean("silence_mode", false) == true

            val chat: SharedPreferences = getSharedPreferences("chat_$chatId", MODE_PRIVATE)

            messages = try {
                val gson = Gson()
                val json = chat.getString("chat", null)
                val type: Type = object : TypeToken<ArrayList<HashMap<String, Any>?>?>() {}.type

                gson.fromJson<Any>(json, type) as ArrayList<HashMap<String, Any>>
            } catch (e: Exception) {
                ArrayList()
            }

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

            adapter = ChatAdapter(messages, this)

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
                            MicrophonePermissionScreen::class.java
                        )
                    )
                }

                isRecording = true
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
            ai = OpenAI(key!!)
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
            val settings: SharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)

            val prompt: String? = settings.getString("prompt", "")

            if (prompt.toString() != "") {
                putMessage(prompt!!, false)

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
        val settings: SharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)
        model = settings.getString("model", "gpt-3.5-turbo").toString()
    }

    @Suppress("unused")
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
        val settings: SharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)
        resolution = settings.getString("resolution", "512x512")!!
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

            putMessage(message, false)
            saveSettings()

            btnMicro?.isEnabled = false
            btnSend?.isEnabled = false
            progress?.visibility = View.VISIBLE

            if (message.lowercase().contains("/imagine") && message.length > 9) {
                val x: String = message.substring(9)

                sendImageRequest(x)
            } else if (message.lowercase().contains("/imagine") && message.length <= 9) {
                putMessage("Prompt can not be empty. Use /imagine &lt;PROMPT&gt;", true)

                saveSettings()

                btnMicro?.isEnabled = true
                btnSend?.isEnabled = true
                progress?.visibility = View.GONE
            } else if (message.lowercase().contains("create an image") ||
                message.lowercase().contains("generate an image") ||
                message.lowercase().contains("create image") ||
                message.lowercase().contains("generate image") ||
                message.lowercase().contains("create a photo") ||
                message.lowercase().contains("generate a photo") ||
                message.lowercase().contains("create photo") ||
                message.lowercase().contains("generate photo")) {
                sendImageRequest(message)
            } else {
                chatMessages.add(ChatMessage(
                    role = ChatRole.User,
                    content = message
                ))

                CoroutineScope(Dispatchers.Main).launch {
                    generateResponse(message, false)
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
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
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
            if (model.contains("davinci") || model.contains("curie") || model.contains("babbage") || model.contains("ada")) {

                val tokens = if (model.contains("text-davinci") || model.contains("code-davinci")) {
                    2048
                } else 1500

                val completionRequest = CompletionRequest(
                    model = ModelId(model),
                    prompt = request,
                    maxTokens = tokens,
                    echo = false
                )

                val completions: Flow<TextCompletion> = ai!!.completions(completionRequest)

                completions.collect { v ->
                    run {
                        if (v.choices[0].text != null) {
                            response += v.choices[0].text
                            messages[messages.size - 1]["message"] = "$response █"
                            adapter?.notifyDataSetChanged()
                        }
                    }
                }
            } else {
                val chatCompletionRequest = ChatCompletionRequest(
                    model = ModelId(model),
                    messages = chatMessages
                )

                val completions: Flow<ChatCompletionChunk> = ai!!.chatCompletions(chatCompletionRequest)

                completions.collect { v ->
                    run {
                        if (v.choices[0].delta != null) {
                            if (v.choices[0].delta?.content != null) {
                                response += v.choices[0].delta?.content
                                messages[messages.size - 1]["message"] = "$response █"
                                adapter?.notifyDataSetChanged()
                            }
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
                tts!!.speak(response, TextToSpeech.QUEUE_FLUSH, null,"")
            }
        } catch (e: Exception) {
            response += "[SYSTEM ERROR] "

            if (e.stackTraceToString().contains("does not exist")) {
                response += "Looks like this model (${model}) is not available to you right now. It can be because of high demand or this model is currently in limited beta."
            } else if (e.stackTraceToString().contains("Connect timeout has expired") || e.stackTraceToString().contains("SocketTimeoutException")) {
                response += "Could not connect to OpenAI servers. It may happen when your Internet speed is slow or too many users are using this model at the same time. Try to switch to another model."
            } else if (e.stackTraceToString().contains("This model's maximum")) {
                response += "Too many tokens. It is an internal error, please report it. Also try to truncate your input. Sometimes it may help."
            } else {
                response += e.stackTraceToString()
            }

            messages[messages.size - 1]["message"] = "${response}\n"
            adapter?.notifyDataSetChanged()
        }

        saveSettings()

        btnMicro?.isEnabled = true
        btnSend?.isEnabled = true
        progress?.visibility = View.GONE
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
            val encoded = Base64.getEncoder().encodeToString(bytes)

            val path = "data:image/png;base64,$encoded"

            putMessage(path, true)
        } catch (e: Exception) {
            if (e.stackTraceToString().contains("Your request was rejected")) {
                putMessage("Your prompt contains inappropriate content and can not be processed. We strive to make AI safe and relevant for everyone.", true)
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