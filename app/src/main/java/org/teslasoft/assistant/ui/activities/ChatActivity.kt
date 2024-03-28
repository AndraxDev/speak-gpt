/**************************************************************************
 * Copyright (c) 2023-2024 Dmytro Ostapenko. All rights reserved.
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


package org.teslasoft.assistant.ui.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Configuration.KEYBOARD_QWERTY
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.FragmentActivity
import com.aallam.openai.api.audio.SpeechRequest
import com.aallam.openai.api.audio.TranscriptionRequest
import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionChunk
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.chat.FunctionMode
import com.aallam.openai.api.chat.Parameters
import com.aallam.openai.api.chat.chatCompletionRequest
import com.aallam.openai.api.completion.CompletionRequest
import com.aallam.openai.api.completion.TextCompletion
import com.aallam.openai.api.file.FileSource
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.image.ImageCreation
import com.aallam.openai.api.image.ImageSize
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.api.logging.Logger
import com.aallam.openai.api.model.Model
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost
import com.aallam.openai.client.RetryStrategy
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import com.google.gson.Gson
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.add
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
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
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.util.Base64
import java.util.Locale
import kotlin.time.Duration.Companion.seconds


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
    private var actionBar: ConstraintLayout? = null
    private var btnBack: ImageButton? = null
    private var keyboardFrame: ConstraintLayout? = null
    private var root: ConstraintLayout? = null
    private var threadLoader: LinearLayout? = null

    // Init chat
    private var messages: ArrayList<HashMap<String, Any>> = arrayListOf()
    private var adapter: ChatAdapter? = null
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
    private var cancelState = false
    private var disableAutoScroll = false

    // init AI
    private var ai: OpenAI? = null
    private var key: String? = null
    private var model = ""
    private var endSeparator = ""
    private var prefix = ""

    // Init DALL-e
    private var resolution = "512x152"

    private var messageCounter = 0

    // Init audio
    private var recognizer: SpeechRecognizer? = null
    private var recorder: MediaRecorder? = null

    // Media player for OpenAI TTS
    private var mediaPlayer: MediaPlayer? = null

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
            if (cancelState) {
                cancelState = false

                btnMicro?.isEnabled = true
                btnSend?.isEnabled = true
                progress?.visibility = View.GONE
                isRecording = false
                btnMicro?.setImageResource(R.drawable.ic_microphone)
            } else {
                isRecording = false
                btnMicro?.setImageResource(R.drawable.ic_microphone)

                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val recognizedText = matches[0]

                    putMessage(prefix + recognizedText + endSeparator, false)

                    chatMessages.add(
                        ChatMessage(
                            role = ChatRole.User,
                            content = prefix + recognizedText + endSeparator
                        )
                    )

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
    }

    override fun onResume() {
        super.onResume()
        preloadAmoled()
        reloadAmoled()
    }

    private fun preloadAmoled() {
        if (isDarkThemeEnabled() && Preferences.getPreferences(this, chatId).getAmoledPitchBlack()) {
            threadLoader?.background = ResourcesCompat.getDrawable(resources, R.color.amoled_accent_50, null)
        } else {
            threadLoader?.setBackgroundColor(SurfaceColors.SURFACE_4.getColor(this))
        }
    }

    private fun reloadAmoled() {
        if (isDarkThemeEnabled() && Preferences.getPreferences(this, chatId).getAmoledPitchBlack()) {
            window.setBackgroundDrawableResource(R.color.amoled_window_background)
            window.statusBarColor = ResourcesCompat.getColor(resources, R.color.amoled_accent_50, theme)
            window.navigationBarColor = ResourcesCompat.getColor(resources, R.color.amoled_accent_100, theme)
            keyboardFrame?.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.amoled_accent_100, theme))
            actionBar?.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.amoled_accent_50, theme))
            activityTitle?.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.amoled_accent_50, theme))
            root?.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.amoled_window_background, theme))
            btnBack?.background = getAmoledAccentDrawable(
                AppCompatResources.getDrawable(
                    this,
                    R.drawable.btn_accent_tonal_v4_amoled
                )!!, this
            )

            btnExport?.background = getAmoledAccentDrawable(
                AppCompatResources.getDrawable(
                    this,
                    R.drawable.btn_accent_tonal_v4_amoled
                )!!, this
            )

            btnSettings?.background = getAmoledAccentDrawable(
                AppCompatResources.getDrawable(
                    this,
                    R.drawable.btn_accent_tonal_v4_amoled
                )!!, this
            )

            messageInput?.background = getAmoledAccentDrawableV2(
                AppCompatResources.getDrawable(
                    this,
                    R.drawable.btn_accent_tonal_selector_v6_amoled
                )!!, this
            )

            btnMicro?.background = getAmoledAccentDrawableV2(
                AppCompatResources.getDrawable(
                    this,
                    R.drawable.btn_accent_tonal_v5_amoled
                )!!, this
            )

            btnSend?.background = getAmoledAccentDrawableV2(
                AppCompatResources.getDrawable(
                    this,
                    R.drawable.btn_accent_tonal_v5_amoled
                )!!, this
            )
        } else {
            window.setBackgroundDrawableResource(R.color.window_background)
            window.statusBarColor = SurfaceColors.SURFACE_4.getColor(this)
            window.navigationBarColor = SurfaceColors.SURFACE_2.getColor(this)
            keyboardFrame?.setBackgroundColor(SurfaceColors.SURFACE_2.getColor(this))
            actionBar?.setBackgroundColor(SurfaceColors.SURFACE_4.getColor(this))
            activityTitle?.setBackgroundColor(SurfaceColors.SURFACE_4.getColor(this))
            root?.setBackgroundColor(SurfaceColors.SURFACE_0.getColor(this))
            btnBack?.background = getDarkAccentDrawable(
                AppCompatResources.getDrawable(
                    this,
                    R.drawable.btn_accent_tonal_v4
                )!!, this
            )

            btnExport?.background = getDarkAccentDrawable(
                AppCompatResources.getDrawable(
                    this,
                    R.drawable.btn_accent_tonal_v4
                )!!, this
            )

            btnSettings?.background = getDarkAccentDrawable(
                AppCompatResources.getDrawable(
                    this,
                    R.drawable.btn_accent_tonal_v4
                )!!, this
            )

            messageInput?.background = getDarkAccentDrawable(
                AppCompatResources.getDrawable(
                    this,
                    R.drawable.btn_accent_tonal_selector_v6
                )!!, this
            )

            btnMicro?.background = getDarkAccentDrawable(
                AppCompatResources.getDrawable(
                    this,
                    R.drawable.btn_accent_tonal_v5
                )!!, this
            )

            btnSend?.background = getDarkAccentDrawable(
                AppCompatResources.getDrawable(
                    this,
                    R.drawable.btn_accent_tonal_v5
                )!!, this
            )
        }
    }

    private fun isDarkThemeEnabled(): Boolean {
        return when (resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            Configuration.UI_MODE_NIGHT_NO -> false
            Configuration.UI_MODE_NIGHT_UNDEFINED -> false
            else -> false
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
                if (v.name == Preferences.getPreferences(this@ChatActivity, chatId).getVoice()) {
                    tts!!.voice = v
                }
            }
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

        mediaPlayer = MediaPlayer()

        setContentView(R.layout.activity_chat)

        threadLoader = findViewById(R.id.thread_loader)
        preloadAmoled()
        threadLoader?.visibility = View.VISIBLE



        Thread {
            languageIdentifier = LanguageIdentification.getClient()

            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)

            runOnUiThread {
                val chatActivityTitle: TextView = findViewById(R.id.chat_activity_title)
                val keyboardInput: LinearLayout = findViewById(R.id.keyboard_input)

                chatActivityTitle.setBackgroundColor(SurfaceColors.SURFACE_4.getColor(this))
                keyboardInput.setBackgroundColor(SurfaceColors.SURFACE_5.getColor(this))

                initChatId()
                initSettings()
            }
        }.start()
    }

    public override fun onDestroy() {
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        if (mediaPlayer!!.isPlaying) {
            mediaPlayer!!.stop()
            mediaPlayer!!.reset()
        }
        super.onDestroy()
    }

    /** SYSTEM INITIALIZATION START **/
    @Suppress("unchecked")
    private fun initSettings() {
        key = Preferences.getPreferences(this, chatId).getApiKey(this)

        endSeparator = Preferences.getPreferences(this, chatId).getEndSeparator()
        prefix = Preferences.getPreferences(this, chatId).getPrefix()

        loadResolution()

        if (key == null) {
            startActivity(Intent(this, WelcomeActivity::class.java).setAction(Intent.ACTION_VIEW))
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
            reloadAmoled()
            initSpeechListener()
            initTTS()
            initLogic()
            initAI()
        }
    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    private fun initUI() {
        btnMicro = findViewById(R.id.btn_micro)
        btnSettings = findViewById(R.id.btn_settings)
        chat = findViewById(R.id.messages)
        messageInput = findViewById(R.id.message_input)
        btnSend = findViewById(R.id.btn_send)
        progress = findViewById(R.id.progress)
        activityTitle = findViewById(R.id.chat_activity_title)
        btnExport = findViewById(R.id.btn_export)
        actionBar = findViewById(R.id.action_bar)
        btnBack = findViewById(R.id.btn_back)
        keyboardFrame = findViewById(R.id.keyboard_frame)
        root = findViewById(R.id.root)

        btnExport?.setImageResource(R.drawable.ic_upload)
        btnBack?.setImageResource(R.drawable.ic_back)

        activityTitle?.text = if (chatName.trim().contains("_autoname_")) "Untitled chat" else chatName

        activityTitle?.isSelected = true

        progress?.visibility = View.GONE

        btnMicro?.setImageResource(R.drawable.ic_microphone)
        btnSettings?.setImageResource(R.drawable.ic_settings)

        btnExport?.background = getDarkAccentDrawable(
            AppCompatResources.getDrawable(
                this,
                R.drawable.btn_accent_tonal_v4
            )!!, this
        )

        btnBack?.background = getDarkAccentDrawable(
            AppCompatResources.getDrawable(
                this,
                R.drawable.btn_accent_tonal_v4
            )!!, this
        )

        btnSettings?.background = getDarkAccentDrawable(
            AppCompatResources.getDrawable(
                this,
                R.drawable.btn_accent_tonal_v4
            )!!, this
        )

        btnBack?.setOnClickListener {
            finish()
        }

        chat?.adapter = adapter
        chat?.dividerHeight = 0

        adapter?.notifyDataSetChanged()

        chat?.setOnTouchListener { _, event -> run {
            if (event.action == MotionEvent.ACTION_SCROLL || event.action == MotionEvent.ACTION_UP) {
                chat?.transcriptMode = ListView.TRANSCRIPT_MODE_DISABLED
                disableAutoScroll = true
            }
            return@setOnTouchListener false
        }}

        Handler(Looper.getMainLooper()).postDelayed({
            val fadeOut: Animation = AnimationUtils.loadAnimation(this, R.anim.fade_out)
            threadLoader?.startAnimation(fadeOut)

            fadeOut.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) { /* UNUSED */ }
                override fun onAnimationEnd(animation: Animation) {
                    runOnUiThread {
                        threadLoader?.visibility = View.GONE
                        threadLoader?.elevation = 0.0f
                    }
                }

                override fun onAnimationRepeat(animation: Animation) { /* UNUSED */ }
            })
        }, 50)
    }

    private fun getDarkAccentDrawable(drawable: Drawable, context: Context) : Drawable {
        DrawableCompat.setTint(DrawableCompat.wrap(drawable), getSurfaceColor(context))
        return drawable
    }

    private fun getAmoledAccentDrawable(drawable: Drawable, context: Context) : Drawable {
        DrawableCompat.setTint(DrawableCompat.wrap(drawable), getAmoledSurfaceColor(context))
        return drawable
    }

    private fun getAmoledAccentDrawableV2(drawable: Drawable, context: Context) : Drawable {
        DrawableCompat.setTint(DrawableCompat.wrap(drawable), getAmoledSurfaceColorV2(context))
        return drawable
    }

    private fun getSurfaceColor(context: Context) : Int {
        return SurfaceColors.SURFACE_5.getColor(context)
    }

    private fun getAmoledSurfaceColor(context: Context) : Int {
        return ResourcesCompat.getColor(context.resources, R.color.amoled_accent_50, null)
    }

    private fun getAmoledSurfaceColorV2(context: Context) : Int {
        return ResourcesCompat.getColor(context.resources, R.color.amoled_accent_300, null)
    }

    private fun initLogic() {
        btnMicro?.setOnClickListener {
            if (Preferences.getPreferences(this, chatId).getAudioModel() == "google") {
                handleGoogleSpeechRecognition()
            } else {
                handleWhisperSpeechRecognition()
            }
        }

        btnMicro?.setOnLongClickListener {
            if (isRecording) {
                cancelState = true
                try {
                    if (mediaPlayer!!.isPlaying) {
                        mediaPlayer!!.stop()
                        mediaPlayer!!.reset()
                    }
                    tts!!.stop()
                } catch (_: java.lang.Exception) {/**/}
                btnMicro?.setImageResource(R.drawable.ic_microphone)
                if (Preferences.getPreferences(this, chatId).getAudioModel() == "google") recognizer?.stopListening()
                isRecording = false
            }

            return@setOnLongClickListener true
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

        messageInput?.setOnKeyListener { v, keyCode, event -> run {
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER && event.isShiftPressed && isHardKB(this) && Preferences.getPreferences(this, chatId).getDesktopMode()) {
                (v as EditText).append("\n")
                return@run true
            } else if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER && isHardKB(this) && Preferences.getPreferences(this, chatId).getDesktopMode()) {
                parseMessage((v as EditText).text.toString())
                return@run true
            } else if (event.action == KeyEvent.ACTION_DOWN && ((keyCode == KeyEvent.KEYCODE_ESCAPE && event.isShiftPressed) || keyCode == KeyEvent.KEYCODE_BACK) && Preferences.getPreferences(this, chatId).getDesktopMode()) {
                finish()
                return@run true
            }
            return@run false
        }}

        if (Preferences.getPreferences(this, chatId).getDesktopMode()) {
            messageInput?.requestFocus()
        }

        btnSettings?.setOnClickListener {
            val i = if (Preferences.getPreferences(this, chatId).getExperimentalUI()) {
                Intent(this, SettingsV2Activity::class.java).setAction(Intent.ACTION_VIEW)
            } else {
                Intent(this, SettingsActivity::class.java).setAction(Intent.ACTION_VIEW)
            }

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

    fun isHardKB(ctx: Context): Boolean {
        return resources.configuration.keyboard == KEYBOARD_QWERTY;
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

                if (!cancelState) {
                    try {
                        prepare()
                    } catch (e: IOException) {
                        btnMicro?.setImageResource(R.drawable.ic_microphone)
                        isRecording = false
                        MaterialAlertDialogBuilder(
                            this@ChatActivity,
                            R.style.App_MaterialAlertDialog
                        )
                            .setTitle("Audio error")
                            .setMessage("Failed to initialize microphone")
                            .setPositiveButton("Close") { _, _ -> }
                            .show()
                    }

                    start()
                } else {
                    cancelState = false
                    btnMicro?.setImageResource(R.drawable.ic_microphone)
                    isRecording = false
                }
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

                if (!cancelState) {
                    try {
                        prepare()
                    } catch (e: IOException) {
                        btnMicro?.setImageResource(R.drawable.ic_microphone)
                        isRecording = false
                        MaterialAlertDialogBuilder(
                            this@ChatActivity,
                            R.style.App_MaterialAlertDialog
                        )
                            .setTitle("Audio error")
                            .setMessage("Failed to initialize microphone")
                            .setPositiveButton("Close") { _, _ -> }
                            .show()
                    }

                    start()
                } else {
                    cancelState = false
                    btnMicro?.setImageResource(R.drawable.ic_microphone)
                    isRecording = false
                }
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

        if (!cancelState) {
            CoroutineScope(Dispatchers.Main).launch {
                processRecording()
            }
        } else {
            cancelState = false
            btnMicro?.setImageResource(R.drawable.ic_microphone)
            isRecording = false
        }
    }

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

            if (transcription.trim() == "") {
                isRecording = false
                btnMicro?.isEnabled = true
                btnSend?.isEnabled = true
                progress?.visibility = View.GONE
                btnMicro?.setImageResource(R.drawable.ic_microphone)
            } else {
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
                    ).setAction(Intent.ACTION_VIEW)
                )
            }
        }
    }

    private fun handleGoogleSpeechRecognition() {
        if (isRecording) {
            try {
                if (mediaPlayer!!.isPlaying) {
                    mediaPlayer!!.stop()
                    mediaPlayer!!.reset()
                }
                tts!!.stop()
            } catch (_: java.lang.Exception) {/**/}
            btnMicro?.setImageResource(R.drawable.ic_microphone)
            recognizer?.stopListening()
            isRecording = false
        } else {
            try {
                if (mediaPlayer!!.isPlaying) {
                    mediaPlayer!!.stop()
                    mediaPlayer!!.reset()
                }
                tts!!.stop()
            } catch (_: java.lang.Exception) {/**/}
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
                    ).setAction(Intent.ACTION_VIEW)
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
            startActivity(Intent(this, WelcomeActivity::class.java).setAction(Intent.ACTION_VIEW))
            finish()
        } else {
            val config = OpenAIConfig(
                token = key!!,
                logging = LoggingConfig(LogLevel.None, Logger.Simple),
                timeout = Timeout(socket = 30.seconds),
                organization = null,
                headers = emptyMap(),
                host = OpenAIHost(Preferences.getPreferences(this, chatId).getCustomHost()),
                proxy = null,
                retry = RetryStrategy()
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
            .setPositiveButton("Close") { _, _ -> }
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

    private fun parseMessage(message: String) {
        try {
            if (mediaPlayer!!.isPlaying) {
                mediaPlayer!!.stop()
                mediaPlayer!!.reset()
            }
            tts!!.stop()
        } catch (_: java.lang.Exception) {/**/}
        if (message != "") {
            messageInput?.setText("")

            keyboardMode = false

            val m = prefix + message + endSeparator

            putMessage(m, false)
            saveSettings()

            btnMicro?.isEnabled = false
            btnSend?.isEnabled = false
            progress?.visibility = View.VISIBLE

            val imagineCommandEnabled: Boolean = Preferences.getPreferences(this, chatId).getImagineCommand()

            if (m.lowercase().contains("/imagine") && m.length > 9 && imagineCommandEnabled) {
                val x: String = m.substring(9)

                sendImageRequest(x)
            } else if (m.lowercase().contains("/imagine") && m.length <= 9 && imagineCommandEnabled) {
                putMessage("Prompt can not be empty. Use /imagine &lt;PROMPT&gt;", true)

                saveSettings()

                btnMicro?.isEnabled = true
                btnSend?.isEnabled = true
                progress?.visibility = View.GONE
            } else {
                chatMessages.add(
                    ChatMessage(
                        role = ChatRole.User,
                        content = m
                    )
                )

                CoroutineScope(Dispatchers.Main).launch {
                    generateResponse(m, false)
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
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

        if (!disableAutoScroll) {
            chat?.post {
                chat?.setSelection(adapter?.count!! - 1)
            }
        }
    }

    private fun generateImages(prompt: String) {
        sendImageRequest(prompt)
    }

    private fun searchInternet(prompt: String) {
        putMessage("Searching at Google...", true)

        saveSettings()

        btnMicro?.isEnabled = true
        btnSend?.isEnabled = true
        progress?.visibility = View.GONE

        val q = prompt.replace(" ", "+")

        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.data = Uri.parse("https://www.google.com/search?q=$q")
        startActivity(intent)
    }

    private suspend fun generateResponse(request: String, shouldPronounce: Boolean) {
        disableAutoScroll = false
        chat?.transcriptMode = ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL
        try {
            var response = ""

            if (!model.contains("gpt") || model.contains(":ft-")) {
                putMessage("", true)
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

                messages[messages.size - 1]["message"] = "$response\n"
                adapter?.notifyDataSetChanged()

                chatMessages.add(ChatMessage(
                    role = ChatRole.Assistant,
                    content = response
                ))

                pronounce(shouldPronounce, response)

                saveSettings()

                btnMicro?.isEnabled = true
                btnSend?.isEnabled = true
                progress?.visibility = View.GONE
                messageInput?.requestFocus()
            } else {
                val functionCallingEnabled: Boolean = Preferences.getPreferences(this, chatId).getFunctionCalling()

                if (functionCallingEnabled) {
                    val imageParams = Parameters.buildJsonObject {
                        put("type", "object")
                        putJsonObject("properties") {
                            putJsonObject("prompt") {
                                put("type", "string")
                                put("description", "The prompt for image generation")
                            }
                        }
                        putJsonArray("required") {
                            add("prompt")
                        }
                    }

                    val searchParams = Parameters.buildJsonObject {
                        put("type", "object")
                        putJsonObject("properties") {
                            putJsonObject("prompt") {
                                put("type", "string")
                                put("description", "Search query")
                            }
                        }
                        putJsonArray("required") {
                            add("prompt")
                        }
                    }

                    val cm = mutableListOf(
                        ChatMessage(
                            role = ChatRole.User,
                            content = request
                        )
                    )

                    val functionRequest = chatCompletionRequest {
                        model = ModelId(this@ChatActivity.model)
                        messages = cm
                        functions {
                            function {
                                name = "generateImages"
                                description = "Generate an image based on the entered prompt"
                                parameters = imageParams
                            }

                            function {
                                name = "searchInternet"
                                description = "Search the Internet"
                                parameters = searchParams
                            }
                        }
                        functionCall = FunctionMode.Auto
                    }

                    val response1 = ai?.chatCompletion(functionRequest)

                    val message = response1?.choices?.first()?.message

                    if (message?.functionCall != null) {
                        val functionCall = message.functionCall!!
                        val imageGenerationAvailable = mapOf("generateImages" to ::generateImages)
                        val searchInternetAvailable = mapOf("searchInternet" to ::searchInternet)
                        val imageGenerationAvailableToCall =
                            imageGenerationAvailable[functionCall.name]
                        val searchInternetAvailableToCall =
                            searchInternetAvailable[functionCall.name]
                        val imageGenerationAvailableArgs =
                            functionCall.argumentsAsJson()
                        val searchInternetAvailableArgs =
                            functionCall.argumentsAsJson()
                        if (imageGenerationAvailableToCall != null) {
                            imageGenerationAvailableToCall(
                                imageGenerationAvailableArgs.getValue("prompt").jsonPrimitive.content
                            )
                        } else if (searchInternetAvailableToCall != null) {
                            searchInternetAvailableToCall(
                                searchInternetAvailableArgs.getValue("prompt").jsonPrimitive.content
                            )
                        } else {
                            regularGPTResponse(shouldPronounce)
                        }
                    } else {
                        regularGPTResponse(shouldPronounce)
                    }
                } else {
                    regularGPTResponse(shouldPronounce)
                }
            }
        } catch (e: Exception) {
            val response = when {
                e.stackTraceToString().contains("does not exist") -> {
                    "Looks like this model (${model}) is not available to you right now. It can be because of high demand or this model is currently in limited beta. If you are using a fine-tuned model, please make sure you entered correct model name. Usually model starts with 'model_name:ft-' and contains original model name, organization name and timestamp. Example: ada:ft-organization_name:model_name-YYYY-MM-DD-hh-mm-ss."
                }
                e.stackTraceToString().contains("Connect timeout has expired") || e.stackTraceToString().contains("SocketTimeoutException") -> {
                    "Could not connect to OpenAI servers. It may happen when your Internet speed is slow or too many users are using this model at the same time. Try to switch to another model."
                }
                e.stackTraceToString().contains("This model's maximum") -> {
                    "Too many tokens. It is an internal error, please report it. Also try to truncate your input. Sometimes it may help."
                }
                e.stackTraceToString().contains("No address associated with hostname") -> {
                    "You are currently offline. Please check your connection and try again."
                }
                e.stackTraceToString().contains("Incorrect API key") -> {
                    "Your API key is incorrect. Change it in Settings > Change OpenAI key. If you think this is an error please check if your API key has not been rotated. If you accidentally published your key it might be automatically revoked."
                }
                e.stackTraceToString().contains("you must provide a model") -> {
                    "No valid model is set in settings. Please change the model and try again."
                }
                e.stackTraceToString().contains("Software caused connection abort") -> {
                    "\n\n[error] An error occurred while generating response. It may be due to a weak connection or high demand. Try to switch to another model or try again later."
                }
                e.stackTraceToString().contains("You exceeded your current quota") -> {
                    "You exceeded your current quota. If you had free trial usage please add payment info. Also please check your usage limits. You can change your limits in Account settings."
                }
                else -> {
                    e.stackTraceToString() + "\n\n" + e.message
                }
            }

            putMessage(response, true)
            adapter?.notifyDataSetChanged()

            saveSettings()

            btnMicro?.isEnabled = true
            btnSend?.isEnabled = true
            progress?.visibility = View.GONE
            messageInput?.requestFocus()
        }
    }

    private suspend fun regularGPTResponse(shouldPronounce: Boolean) {
        disableAutoScroll = false
        chat?.transcriptMode = ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL
        var response = ""
        putMessage("", true)

        val msgs: ArrayList<ChatMessage> = chatMessages.clone() as ArrayList<ChatMessage>

        val systemMessage = Preferences.getPreferences(this, chatId).getSystemMessage()

        if (systemMessage != "") {
            msgs.add(
                ChatMessage(
                    role = ChatRole.System,
                    content = systemMessage
                )
            )
        }

        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId(model),
            messages = msgs
        )

        val completions: Flow<ChatCompletionChunk> =
            ai!!.chatCompletions(chatCompletionRequest)

        completions.collect { v ->
            run {
                if (v.choices[0].delta.content != null) {
                    response += v.choices[0].delta.content
                    messages[messages.size - 1]["message"] = "$response █"
                    adapter?.notifyDataSetChanged()
                }
            }
        }

        messages[messages.size - 1]["message"] = "$response\n"
        adapter?.notifyDataSetChanged()

        chatMessages.add(
            ChatMessage(
                role = ChatRole.Assistant,
                content = response
            )
        )

        pronounce(shouldPronounce, response)

        saveSettings()

        btnMicro?.isEnabled = true
        btnSend?.isEnabled = true
        progress?.visibility = View.GONE
        messageInput?.requestFocus()

        if (messageCounter == 0) {
            val chatName = ChatPreferences.getChatPreferences().getChatName(this, chatId)

            if (chatName.trim().contains("_autoname_")) {
                btnMicro?.isEnabled = false
                btnSend?.isEnabled = false
                progress?.visibility = View.GONE
                messageInput?.requestFocus()

                val m = msgs

                m.add(
                    ChatMessage(
                        role = ChatRole.User,
                        content = "Create a short name for this chat according to the messages provided. Enter just short name and nothing else. Don't add word 'chat' or 'bot' to the name."
                    )
                )

                val chatCompletionRequest2 = ChatCompletionRequest(
                    model = ModelId("gpt-3.5-turbo-0125"),
                    maxTokens = 5,
                    messages = m
                )

                val completion: ChatCompletion = ai!!.chatCompletion(chatCompletionRequest2)

                val newChatName = completion.choices[0].message.content

                ChatPreferences.getChatPreferences().editChat(this, newChatName.toString(), chatName)
                chatId = Hash.hash(newChatName.toString())

                val preferences = Preferences.getPreferences(this, Hash.hash(chatName))

                // Write settings
                val resolution = preferences.getResolution()
                val speech = preferences.getAudioModel()
                val model = preferences.getModel()
                val maxTokens = preferences.getMaxTokens()
                val prefix = preferences.getPrefix()
                val endSeparator = preferences.getEndSeparator()
                val activationPrompt = preferences.getPrompt()
                val layout = preferences.getLayout()
                val silent = preferences.getSilence()
                val systemMessage1 = preferences.getSystemMessage()
                val alwaysSpeak = preferences.getNotSilence()
                val autoLanguageDetect = preferences.getAutoLangDetect()
                val functionCalling = preferences.getFunctionCalling()
                val slashCommands = preferences.getImagineCommand()

                preferences.setPreferences(Hash.hash(newChatName.toString()), this)
                preferences.setResolution(resolution)
                preferences.setAudioModel(speech)
                preferences.setModel(model)
                preferences.setMaxTokens(maxTokens)
                preferences.setPrefix(prefix)
                preferences.setEndSeparator(endSeparator)
                preferences.setPrompt(activationPrompt)
                preferences.setLayout(layout)
                preferences.setSilence(silent)
                preferences.setSystemMessage(systemMessage1)
                preferences.setNotSilence(alwaysSpeak)
                preferences.setAutoLangDetect(autoLanguageDetect)
                preferences.setFunctionCalling(functionCalling)
                preferences.setImagineCommand(slashCommands)

                activityTitle?.text = newChatName.toString()

                val i = Intent(this, ChatActivity::class.java).setAction(Intent.ACTION_VIEW).putExtra("chatId", Hash.hash(newChatName.toString())).putExtra("name", newChatName.toString())
                startActivity(i)
                finish()
            }
        }

        messageCounter++
    }

    private fun pronounce(st: Boolean, message: String) {
        if ((st && isTTSInitialized && !silenceMode) || Preferences.getPreferences(this, chatId).getNotSilence()) {
            if (autoLangDetect) {
                languageIdentifier.identifyLanguage(message)
                    .addOnSuccessListener { languageCode ->
                        if (languageCode == "und") {
                            Log.i("MLKit", "Can't identify language.")
                        } else {
                            Log.i("MLKit", "Language: $languageCode")
                            tts!!.language = Locale.forLanguageTag(
                                languageCode
                            )
                        }

                        speak(message)
                    }.addOnFailureListener {
                        // Ignore auto language detection if an error is occurred
                        autoLangDetect = false
                        ttsPostInit()

                        speak(message)
                    }
            } else {
                speak(message)
            }
        }
    }

    private fun speak(message: String) {
        val preferences = Preferences.getPreferences(this, chatId)
        val preferences2 = Preferences.getPreferences(this, chatId)

        if (preferences.getTtsEngine() == "google") {
            tts!!.speak(message, TextToSpeech.QUEUE_FLUSH, null, "")
        } else {
            CoroutineScope(Dispatchers.Main).launch {
                val rawAudio = ai!!.speech(
                    request = SpeechRequest(
                        model = ModelId("tts-1"),
                        input = message,
                        voice = com.aallam.openai.api.audio.Voice(preferences2.getOpenAIVoice()),
                    )
                )

                runOnUiThread {
                    try {
                        // create temp file that will hold byte array
                        val tempMp3 = File.createTempFile("audio", "mp3", cacheDir)
                        tempMp3.deleteOnExit()
                        val fos = FileOutputStream(tempMp3)
                        fos.write(rawAudio)
                        fos.close()

                        // resetting media player instance to evade problems
                        mediaPlayer?.reset()

                        // In case you run into issues with threading consider new instance like:
                        // MediaPlayer mediaPlayer = new MediaPlayer();

                        // Tried passing path directly, but kept getting
                        // "Prepare failed.: status=0x1"
                        // so using file descriptor instead
                        val fis = FileInputStream(tempMp3)
                        mediaPlayer?.setDataSource(fis.getFD())
                        mediaPlayer?.prepare()
                        mediaPlayer?.start()
                    } catch (ex: IOException) {
                        MaterialAlertDialogBuilder(this@ChatActivity, R.style.App_MaterialAlertDialog)
                            .setTitle("Audio error")
                            .setMessage(ex.stackTraceToString())
                            .setPositiveButton("Close") { _, _ -> }
                            .show()
                    }
                }
            }
        }
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

    @SuppressLint("ClickableViewAccessibility")
    private suspend fun generateImage(p: String) {
        chat?.setOnTouchListener(null)
        disableAutoScroll = false
        chat?.transcriptMode = ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL
        try {
            val images = ai?.imageURL(
                creation = ImageCreation(
                    prompt = p,
                    model = ModelId("dall-e-${Preferences.getPreferences(this, chatId).getDalleVersion()}"),
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

            chat?.setOnTouchListener { _, event -> run {
                if (event.action == MotionEvent.ACTION_SCROLL || event.action == MotionEvent.ACTION_UP) {
                    chat?.transcriptMode = ListView.TRANSCRIPT_MODE_DISABLED
                    disableAutoScroll = true
                }
                return@setOnTouchListener false
            }}
        } catch (e: Exception) {
            putMessage(
                when {
                    e.stackTraceToString().contains("Your request was rejected") -> {
                        "Your prompt contains inappropriate content and can not be processed. We strive to make AI safe and relevant for everyone."
                    }

                    e.stackTraceToString().contains("No address associated with hostname") -> {
                        "You are currently offline. Please check your connection and try again.";
                    }

                    e.stackTraceToString().contains("Incorrect API key") -> {
                        "Your API key is incorrect. Change it in Settings > Change OpenAI key. If you think this is an error please check if your API key has not been rotated. If you accidentally published your key it might be automatically revoked.";
                    }

                    e.stackTraceToString().contains("Software caused connection abort") -> {
                        "An error occurred while generating response. It may be due to a weak connection or high demand. Try again later.";
                    }

                    e.stackTraceToString().contains("You exceeded your current quota") -> {
                        "You exceeded your current quota. If you had free trial usage please add payment info. Also please check your usage limits. You can change your limits in Account settings."
                    }

                    else -> {
                        e.stackTraceToString()
                    }
                }, true
            )
        }

        saveSettings()

        btnMicro?.isEnabled = true
        btnSend?.isEnabled = true
        progress?.visibility = View.GONE

        messageInput?.requestFocus()
    }
}
