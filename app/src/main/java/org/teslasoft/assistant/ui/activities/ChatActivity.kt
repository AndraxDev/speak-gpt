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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import android.widget.ImageView
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
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.util.Locale
import kotlin.time.Duration.Companion.seconds
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.PorterDuff
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import androidx.core.content.FileProvider
import com.aallam.openai.api.chat.ContentPart
import com.aallam.openai.api.chat.ImagePart
import com.aallam.openai.api.chat.TextPart
import com.aallam.openai.api.core.Role
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import org.teslasoft.assistant.preferences.GlobalPreferences
import org.teslasoft.assistant.ui.permission.CameraPermissionActivity
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.CancellationException
import org.teslasoft.assistant.preferences.ApiEndpointPreferences
import org.teslasoft.assistant.preferences.LogitBiasPreferences
import org.teslasoft.assistant.preferences.dto.ApiEndpointObject
import org.teslasoft.assistant.ui.adapters.AbstractChatAdapter
import org.teslasoft.assistant.ui.fragments.dialogs.QuickSettingsBottomSheetDialogFragment
import kotlin.coroutines.coroutineContext


class ChatActivity : FragmentActivity(), AbstractChatAdapter.OnUpdateListener {

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
    private var btnAttachFile: ImageButton? = null
    private var attachedImage: LinearLayout? = null
    private var selectedImage: ImageView? = null
    private var btnRemoveImage: ImageButton? = null
    private var visionActions: LinearLayout? = null
    private var btnVisionActionCamera: ImageButton? = null
    private var btnVisionActionGallery: ImageButton? = null

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
    private var imageIsSelected = false

    // init AI
    private var ai: OpenAI? = null
    private var key: String? = null
    private var model = ""
    private var endSeparator = ""
    private var prefix = ""
    private var apiEndpointPreferences: ApiEndpointPreferences? = null
    private var logitBiasPreferences: LogitBiasPreferences? = null
    private var apiEndpointObject: ApiEndpointObject? = null

    private var stopper = false

    // Init DALL-e
    private var resolution = "512x152"

    private var messageCounter = 0

    // Init audio
    private var recognizer: SpeechRecognizer? = null
    private var recorder: MediaRecorder? = null

    // Media player for OpenAI TTS
    private var mediaPlayer: MediaPlayer? = null

    // Init preferences
    private var preferences: Preferences? = null
    private var preferencesChangedListener: Preferences.PreferencesChangedListener? = null

    private var onSpeechResultsScope: CoroutineScope? = null
    private var whisperScope: CoroutineScope? = null
    private var processRecordingScope: CoroutineScope? = null
    private var setupScope: CoroutineScope? = null
    private var imageRequestScope: CoroutineScope? = null
    private var speakScope: CoroutineScope? = null

    private fun killAllProcesses() {
        onSpeechResultsScope?.coroutineContext?.cancel(CancellationException("Killed"))
        whisperScope?.coroutineContext?.cancel(CancellationException("Killed"))
        processRecordingScope?.coroutineContext?.cancel(CancellationException("Killed"))
        setupScope?.coroutineContext?.cancel(CancellationException("Killed"))
        imageRequestScope?.coroutineContext?.cancel(CancellationException("Killed"))
        speakScope?.coroutineContext?.cancel(CancellationException("Killed"))
    }

    private fun restoreUIState() {
        progress?.visibility = View.GONE
        btnMicro?.isEnabled = true
        btnSend?.isEnabled = true
        isRecording = false
        btnMicro?.setImageResource(R.drawable.ic_microphone)
    }

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

                    if (preferences?.autoSend() == true) {
                        onSpeechResultsScope = CoroutineScope(Dispatchers.Main)
                        onSpeechResultsScope?.launch {
                            progress?.setOnClickListener {
                                cancel()
                                restoreUIState()
                            }

                            try {
                                generateResponse(prefix + recognizedText + endSeparator, true)
                            } catch (e: CancellationException) {
                                Toast.makeText(this@ChatActivity, "Cancelled", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        restoreUIState()
                        messageInput?.setText(recognizedText)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        preloadAmoled()
        reloadAmoled()

        if (chatId != "") {
            preferences = Preferences.getPreferences(this, chatId)
            apiEndpointPreferences = ApiEndpointPreferences.getApiEndpointPreferences(this)
            logitBiasPreferences = LogitBiasPreferences(this, preferences?.getLogitBiasesConfigId()!!)
            apiEndpointObject = apiEndpointPreferences?.getApiEndpoint(this, preferences?.getApiEndpointId()!!)
        }
    }

    private fun preloadAmoled() {
        if (isDarkThemeEnabled() && GlobalPreferences.getPreferences(this).getAmoledPitchBlack()) {
            threadLoader?.background = ResourcesCompat.getDrawable(resources, R.color.amoled_accent_50, null)
        } else {
            threadLoader?.setBackgroundColor(SurfaceColors.SURFACE_4.getColor(this))
        }
    }

    private var cameraIntentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "tmp.jpg")
            val uri = FileProvider.getUriForFile(this, "org.teslasoft.assistant.fileprovider", imageFile)

            bitmap = readFile(uri)

            if (bitmap != null) {
                attachedImage?.visibility = View.VISIBLE
                selectedImage?.setImageBitmap(roundCorners(bitmap!!, 80f))
                imageIsSelected = true

                val mimeType = contentResolver.getType(uri)
                val format = when {
                    mimeType.equals("image/png", ignoreCase = true) -> {
                        selectedImageType = "png"
                        Bitmap.CompressFormat.PNG
                    }
                    else -> {
                        selectedImageType = "jpg"
                        Bitmap.CompressFormat.JPEG
                    }
                }

                // Step 3: Convert the Bitmap to a Base64-encoded string
                val outputStream = ByteArrayOutputStream()
                bitmap!!.compress(format, 100, outputStream) // Note: Adjust the quality as necessary
                val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)

                // Step 4: Generate the data URL
                val imageType = when(format) {
                    Bitmap.CompressFormat.JPEG -> "jpeg"
                    Bitmap.CompressFormat.PNG -> "png"
                    // Add more mappings as necessary
                    else -> ""
                }

                baseImageString = "data:image/$imageType;base64,$base64Image"
            }
        }
    }

    private val permissionResultLauncherCamera = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        run {
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = Intent().setAction(MediaStore.ACTION_IMAGE_CAPTURE)
                intent.putExtra("android.intent.extra.quickCapture", true)
                val externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                val imageFile = File(externalFilesDir, "tmp.jpg")
                intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this, "org.teslasoft.assistant.fileprovider", imageFile))
                cameraIntentLauncher.launch(intent)
            }
        }
    }

    private fun reloadAmoled() {
        if (isDarkThemeEnabled() && GlobalPreferences.getPreferences(this).getAmoledPitchBlack()) {
            window.setBackgroundDrawableResource(R.color.amoled_window_background)
            window.statusBarColor = ResourcesCompat.getColor(resources, R.color.amoled_accent_100, theme)
            window.navigationBarColor = ResourcesCompat.getColor(resources, R.color.amoled_accent_100, theme)
            keyboardFrame?.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.amoled_accent_100, theme))
            actionBar?.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.amoled_accent_100, theme))
            activityTitle?.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.amoled_accent_100, theme))
            root?.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.amoled_window_background, theme))
            messageInput?.setHintTextColor(ResourcesCompat.getColor(resources, R.color.amoled_accent_600, theme))
            btnBack?.background = getAmoledAccentDrawable(
                AppCompatResources.getDrawable(
                    this,
                    R.drawable.btn_accent_tonal_v5_amoled
                )!!, this
            )

            btnExport?.background = getAmoledAccentDrawable(
                AppCompatResources.getDrawable(
                    this,
                    R.drawable.btn_accent_tonal_v5_amoled
                )!!, this
            )

            btnSettings?.background = getAmoledAccentDrawable(
                AppCompatResources.getDrawable(
                    this,
                    R.drawable.btn_accent_tonal_v5_amoled
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
            btnAttachFile?.background = getAmoledAccentDrawableV2(
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
            messageInput?.setHintTextColor(ResourcesCompat.getColor(resources, R.color.accent_500, theme))
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
            btnAttachFile?.background = getDarkAccentDrawable(
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
                    preferences!!.getLanguage()
                )
            )

            isTTSInitialized =
                !(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)

            val voices: Set<Voice> = tts!!.voices
            for (v: Voice in voices) {
                if (v.name == preferences!!.getVoice()) {
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

        setContentView(R.layout.activity_chat)

        preloadAmoled()
        reloadAmoled()

        mediaPlayer = MediaPlayer()

        threadLoader = findViewById(R.id.thread_loader)

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

        killAllProcesses()

        super.onDestroy()
    }

    /** SYSTEM INITIALIZATION START **/
    @Suppress("unchecked")
    private fun initSettings() {
        key = apiEndpointObject?.apiKey!!

        endSeparator = preferences!!.getEndSeparator()
        prefix = preferences!!.getPrefix()

        loadResolution()

        if (key == null) {
            startActivity(Intent(this, WelcomeActivity::class.java).setAction(Intent.ACTION_VIEW))
            finish()
        } else {
            silenceMode = preferences!!.getSilence()
            autoLangDetect = preferences!!.getAutoLangDetect()
            messages = ChatPreferences.getChatPreferences().getChatById(this, chatId)

            // R8 fix
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

            adapter = ChatAdapter(messages, this, preferences!!)
            adapter?.setOnUpdateListener(this)

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
        btnAttachFile = findViewById(R.id.btn_attach)
        attachedImage = findViewById(R.id.attachedImage)
        selectedImage = findViewById(R.id.selectedImage)
        btnRemoveImage = findViewById(R.id.btnRemoveImage)
        visionActions = findViewById(R.id.vision_action_selector)
        btnVisionActionCamera = findViewById(R.id.action_camera)
        btnVisionActionGallery = findViewById(R.id.action_gallery)

        visionActions?.visibility = View.GONE

        attachedImage?.visibility = View.GONE

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

        activityTitle?.setOnClickListener {
            val quickSettingsBottomSheetDialogFragment = QuickSettingsBottomSheetDialogFragment.newInstance(chatId)
            quickSettingsBottomSheetDialogFragment.setOnUpdateListener(object : QuickSettingsBottomSheetDialogFragment.OnUpdateListener {
                override fun onUpdate() {
                    /* for future */
                }

                override fun onForceUpdate() {
                    startActivity(Intent(this@ChatActivity, ChatActivity::class.java).putExtra("chatId", chatId).putExtra("name", chatName).setAction(Intent.ACTION_VIEW))
                    finish()
                }
            })
            quickSettingsBottomSheetDialogFragment.show(supportFragmentManager, "QuickSettingsBottomSheetDialogFragment")
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
        return SurfaceColors.SURFACE_4.getColor(context)
    }

    private fun getAmoledSurfaceColor(context: Context) : Int {
        return ResourcesCompat.getColor(context.resources, R.color.amoled_accent_100, null)
    }

    private fun getAmoledSurfaceColorV2(context: Context) : Int {
        return ResourcesCompat.getColor(context.resources, R.color.amoled_accent_200, null)
    }

    private var bitmap: Bitmap? = null
    private var baseImageString: String? = null
    private var selectedImageType: String? = null

    private fun roundCorners(bitmap: Bitmap, cornerRadius: Float): Bitmap {
        // Create a bitmap with the same size as the original.
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)

        // Prepare a canvas with the new bitmap.
        val canvas = Canvas(output)

        // The paint used to draw the original bitmap onto the new one.
        val paint = Paint().apply {
            isAntiAlias = true
            color = -0xbdbdbe
        }

        // The rectangle bounds for the original bitmap.
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = RectF(rect)

        // Draw rounded rectangle as background.
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)

        // Change the paint mode to draw the original bitmap on top.
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        // Draw the original bitmap.
        canvas.drawBitmap(bitmap, rect, rect, paint)

        return output
    }

    private val fileIntentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        run {
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.also { uri ->
                    bitmap = readFile(uri)

                    if (bitmap != null) {
                        attachedImage?.visibility = View.VISIBLE
                        selectedImage?.setImageBitmap(roundCorners(bitmap!!, 80f))
                        imageIsSelected = true

                        val mimeType = contentResolver.getType(uri)
                        val format = when {
                            mimeType.equals("image/png", ignoreCase = true) -> {
                                selectedImageType = "png"
                                Bitmap.CompressFormat.PNG
                            }
                            else -> {
                                selectedImageType = "jpg"
                                Bitmap.CompressFormat.JPEG
                            }
                        }

                        // Step 3: Convert the Bitmap to a Base64-encoded string
                        val outputStream = ByteArrayOutputStream()
                        bitmap!!.compress(format, 100, outputStream) // Note: Adjust the quality as necessary
                        val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)

                        // Step 4: Generate the data URL
                        val imageType = when(format) {
                            Bitmap.CompressFormat.JPEG -> "jpeg"
                            Bitmap.CompressFormat.PNG -> "png"
                            // Add more mappings as necessary
                            else -> ""
                        }

                        baseImageString = "data:image/$imageType;base64,$base64Image"
                    }
                }
            }
        }
    }

    private fun readFile(uri: Uri) : Bitmap? {
        return contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { _ ->
                BitmapFactory.decodeStream(inputStream)
            }
        }
    }

    private fun openFile(pickerInitialUri: Uri) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"

            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }

        fileIntentLauncher.launch(intent)
    }

    private fun initLogic() {
        btnMicro?.setOnClickListener {
            if (preferences!!.getAudioModel() == "google") {
                handleGoogleSpeechRecognition()
            } else {
                handleWhisperSpeechRecognition()
            }
        }

        attachedImage?.setOnClickListener { /* ignored */ }

        btnMicro?.setOnLongClickListener {
            if (isRecording) {
                cancelState = true
                try {
                    if (mediaPlayer!!.isPlaying) {
                        mediaPlayer!!.stop()
                        mediaPlayer!!.reset()
                    }
                    tts!!.stop()
                } catch (_: java.lang.Exception) {/* ignored */}
                btnMicro?.setImageResource(R.drawable.ic_microphone)
                if (preferences!!.getAudioModel() == "google") recognizer?.stopListening()
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

        btnAttachFile?.setOnClickListener {
            visionActions?.visibility = if (visionActions?.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        btnVisionActionGallery?.setOnClickListener {
            visionActions?.visibility = View.GONE
            openFile(Uri.parse("/storage/emulated/0/image.png"))
        }

        btnVisionActionCamera?.setOnClickListener {
            visionActions?.visibility = View.GONE
            val intent = Intent(this, CameraPermissionActivity::class.java).setAction(Intent.ACTION_VIEW)
            permissionResultLauncherCamera.launch(intent)
        }

        btnRemoveImage?.setOnClickListener {
            attachedImage?.visibility = View.GONE
            imageIsSelected = false
            bitmap = null
        }

        messageInput?.setOnKeyListener { v, keyCode, event -> run {
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER && event.isShiftPressed && isHardKB(this) && preferences!!.getDesktopMode()) {
                (v as EditText).append("\n")
                return@run true
            } else if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER && isHardKB(this) && preferences!!.getDesktopMode()) {
                parseMessage((v as EditText).text.toString())
                return@run true
            } else if (event.action == KeyEvent.ACTION_DOWN && ((keyCode == KeyEvent.KEYCODE_ESCAPE && event.isShiftPressed) || keyCode == KeyEvent.KEYCODE_BACK) && preferences!!.getDesktopMode()) {
                finish()
                return@run true
            }
            return@run false
        }}

        if (preferences!!.getDesktopMode()) {
            messageInput?.requestFocus()
        }

        btnSettings?.setOnClickListener {
            settingsLauncher.launch(
                Intent(this, SettingsV2Activity::class.java).setAction(Intent.ACTION_VIEW).putExtra("chatId", chatId)
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
            whisperScope = CoroutineScope(Dispatchers.Main)

            whisperScope?.launch {
                progress?.setOnClickListener {
                    cancel()
                    restoreUIState()
                }

                try {
                    processRecording()
                } catch (e: CancellationException) {
                    Toast.makeText(this@ChatActivity, "Cancelled", Toast.LENGTH_SHORT).show()
                }
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
                if (preferences?.autoSend() == true) {
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

                    processRecordingScope = CoroutineScope(Dispatchers.Main)

                    processRecordingScope?.launch {
                        progress?.setOnClickListener {
                            cancel()
                            restoreUIState()
                        }

                        try {
                            generateResponse(prefix + transcription + endSeparator, true)
                        } catch (cancelledException: CancellationException) {
                            Toast.makeText(this@ChatActivity, "Cancelled", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    restoreUIState()
                    messageInput?.setText(transcription)
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
                host = OpenAIHost(apiEndpointObject?.host!!),
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

        preferences = Preferences.getPreferences(this, chatId)
        apiEndpointPreferences = ApiEndpointPreferences.getApiEndpointPreferences(this)
        logitBiasPreferences = LogitBiasPreferences(this, preferences?.getLogitBiasesConfigId()!!)
        apiEndpointObject = apiEndpointPreferences?.getApiEndpoint(this, preferences?.getApiEndpointId()!!)

        preloadAmoled()
        reloadAmoled()
    }

    /*
    * Setup SpeakGPT with activation prompt.
    * */
    private fun setup() {
        if (messages.isEmpty()) {
            val prompt: String = preferences!!.getPrompt()

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

                setupScope = CoroutineScope(Dispatchers.Main)

                setupScope?.launch {
                    progress?.setOnClickListener {
                        cancel()
                        restoreUIState()
                    }

                    try {
                        generateResponse(prompt, false)
                    } catch (cancelledException: CancellationException) {
                        Toast.makeText(this@ChatActivity, "Cancelled", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun loadModel() {
        model = preferences!!.getModel()
        endSeparator = preferences!!.getEndSeparator()
        prefix = preferences!!.getPrefix()
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
        resolution = preferences!!.getResolution()
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

    private fun parseMessage(message: String, shouldAdd: Boolean = true) {
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

            if (imageIsSelected) {
                val bytes = Base64.decode(baseImageString!!.split(",")[1], Base64.DEFAULT)
                writeImageToCache(bytes, selectedImageType!!)

                val encoded = java.util.Base64.getEncoder().encodeToString(bytes)

                val file = Hash.hash(encoded)

                putMessage(m, false, file, selectedImageType!!)
            } else {
                if (shouldAdd) putMessage(m, false)
            }
            saveSettings()

            btnMicro?.isEnabled = false
            btnSend?.isEnabled = false
            progress?.visibility = View.VISIBLE

            val imagineCommandEnabled: Boolean = preferences!!.getImagineCommand()

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
                if (shouldAdd) {
                    chatMessages.add(
                        ChatMessage(
                            role = ChatRole.User,
                            content = m
                        )
                    )
                }

                CoroutineScope(Dispatchers.Main).launch {
                    progress?.setOnClickListener {
                        cancel()
                        restoreUIState()
                    }

                    try {
                        generateResponse(m, false)
                    } catch (cancelledException: CancellationException) {
                        Toast.makeText(this@ChatActivity, "Cancelled", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun sendImageRequest(str: String) {
        imageRequestScope = CoroutineScope(Dispatchers.Main)
        imageRequestScope?.launch {
            progress?.setOnClickListener {
                cancel()
                restoreUIState()
            }

            try {
                generateImage(str)
            } catch (cancelledException: CancellationException) {
                Toast.makeText(this@ChatActivity, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, LocaleParser.parse(preferences!!.getLanguage()))
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)

        recognizer?.startListening(intent)
    }

    private fun putMessage(message: String, isBot: Boolean, image: String = "", imageType: String = "") {
        val map: HashMap<String, Any> = HashMap()

        map["message"] = message
        map["isBot"] = isBot

        if (image != "") {
            map["image"] = image
            map["imageType"] = imageType
        }

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

            if (imageIsSelected) {
                imageIsSelected = false;

                attachedImage?.visibility = View.GONE

                putMessage("", true)

                val reqList: ArrayList<ContentPart> = ArrayList<ContentPart>()
                reqList.add(TextPart(request))
                reqList.add(ImagePart(baseImageString!!))
                val chatCompletionRequest = ChatCompletionRequest(
                    model = ModelId("gpt-4-vision-preview"),
                    temperature = preferences!!.getTemperature().toDouble(),
                    topP = preferences!!.getTopP().toDouble(),frequencyPenalty = preferences!!.getFrequencyPenalty().toDouble(),
                    presencePenalty = preferences!!.getPresencePenalty().toDouble(),

                    seed = if (preferences!!.getSeed() != "") preferences!!.getSeed().toInt() else null,
                    messages = listOf(
                        ChatMessage(
                            role = ChatRole.System,
                            content = "You are a helpful assistant!"
                        ),
                        ChatMessage(
                            role = ChatRole.User,
                            content = reqList
                        )
                    )
                )

                val completions: Flow<ChatCompletionChunk> = ai!!.chatCompletions(chatCompletionRequest)

                completions.collect { v ->
                    run {
                        if (!coroutineContext.isActive) throw CancellationException()
                        else if (v.choices[0].delta.content != "null") {
                            response += v.choices[0].delta.content
                            if (response != "null") {
                                messages[messages.size - 1]["message"] = response
                                adapter?.notifyDataSetChanged()
                                saveSettings()
                            }
                        }
                    }
                }

                messages[messages.size - 1]["message"] = "${response.dropLast(4)}\n"
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
            } else if (model.contains(":ft") || model.contains("ft:")) {
                putMessage("", true)
                val completionRequest = CompletionRequest(
                    model = ModelId(model),
                    temperature = preferences!!.getTemperature().toDouble(),
                    topP = preferences!!.getTopP().toDouble(),
                    frequencyPenalty = preferences!!.getFrequencyPenalty().toDouble(),
                    presencePenalty = preferences!!.getPresencePenalty().toDouble(),
                    prompt = request,
                    echo = false
                )

                val completions: Flow<TextCompletion> = ai!!.completions(completionRequest)

                completions.collect { v ->
                    run {
                        if (!coroutineContext.isActive) throw CancellationException()
                        else if (v.choices[0].text != "null") {
                            response += v.choices[0].text
                            messages[messages.size - 1]["message"] = response
                            adapter?.notifyDataSetChanged()
                            saveSettings()
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
                val functionCallingEnabled: Boolean = preferences!!.getFunctionCalling()

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
                        temperature = preferences!!.getTemperature().toDouble()
                        topP = preferences!!.getTopP().toDouble()
                        frequencyPenalty = preferences!!.getFrequencyPenalty().toDouble()
                        presencePenalty = preferences!!.getPresencePenalty().toDouble()
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
        } catch (e: CancellationException) {
            runOnUiThread {
                restoreUIState()
            }
        } catch (e: Exception) {
            val response = when {
                e.stackTraceToString().contains("does not exist") -> {
                    "Looks like this model (${model}) is not available to you right now. It can be because of high demand or this model is currently in limited beta. If you are using a fine-tuned model, please make sure you entered correct model name. Usually model starts with 'model_name:ft-' and contains original model name, organization name and timestamp. Example: ada:ft-organization_name:model_name-YYYY-MM-DD-hh-mm-ss."
                }
                e.stackTraceToString().contains("Connect timeout has expired") || e.stackTraceToString().contains("SocketTimeoutException") -> {
                    "Could not connect to the server. It may happen when your Internet speed is slow or too many users are using this model at the same time. Try to switch to another model."
                }
                e.stackTraceToString().contains("This model's maximum") -> {
                    "Too many tokens. It is an internal error, please report it. Also try to truncate your input. Sometimes it may help."
                }
                e.stackTraceToString().contains("No address associated with hostname") -> {
                    "You are currently offline. Please check your connection and try again."
                }
                e.stackTraceToString().contains("Incorrect API key") -> {
                    "Your API key is incorrect. Change it in Settings > Change API key. If you think this is an error please check if your API key has not been rotated. If you accidentally published your key it might be automatically revoked."
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

            if (preferences?.showChatErrors() == true) {
                messages[messages.size - 1]["message"] = "${messages[messages.size - 1]["message"]}\n\nAn error has been occurred during generation. See the error details below:\n\n$response"
                adapter?.notifyDataSetChanged()
            }

            saveSettings()

            runOnUiThread {
                btnMicro?.isEnabled = true
                btnSend?.isEnabled = true
                progress?.visibility = View.GONE
                messageInput?.requestFocus()
            }
        } finally {
            runOnUiThread {
                restoreUIState()
            }
        }
    }

    private suspend fun regularGPTResponse(shouldPronounce: Boolean) {
        disableAutoScroll = false
        chat?.transcriptMode = ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL
        var response = ""
        putMessage("", true)

        val msgs: ArrayList<ChatMessage> = chatMessages.clone() as ArrayList<ChatMessage>

        val systemMessage = preferences!!.getSystemMessage()

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
            temperature = preferences!!.getTemperature().toDouble(),
            topP = preferences!!.getTopP().toDouble(),
            frequencyPenalty = preferences!!.getFrequencyPenalty().toDouble(),
            presencePenalty = preferences!!.getPresencePenalty().toDouble(),
            seed = if (preferences!!.getSeed() != "") preferences!!.getSeed().toInt() else null,
            messages = msgs
        )

        val completions: Flow<ChatCompletionChunk> =
            ai!!.chatCompletions(chatCompletionRequest)

        completions.collect { v ->
            run {
                if (!coroutineContext.isActive) throw CancellationException()
                else if (v.choices[0].delta.content != null) {
                    response += v.choices[0].delta.content
                    messages[messages.size - 1]["message"] = response
                    adapter?.notifyDataSetChanged()
                    saveSettings()
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

                val m = ArrayList(msgs.filter { it.role != ChatRole.System })

                m.add(
                    ChatMessage(
                        role = ChatRole.User,
                        content = "Create a short name for this chat according to the messages provided. Enter just short name and nothing else. Don't add word 'chat' or 'bot' to the name."
                    )
                )

                val chatCompletionRequest2 = ChatCompletionRequest(
                    model = ModelId("gpt-3.5-turbo-0125"),
                    maxTokens = 10,
                    messages = m
                )

                try {
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
                    val apiEndpointId = preferences.getApiEndpointId()

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
                    preferences.setApiEndpointId(apiEndpointId)

                    activityTitle?.text = newChatName.toString()

                    val i = Intent(this, ChatActivity::class.java).setAction(Intent.ACTION_VIEW).putExtra("chatId", Hash.hash(newChatName.toString())).putExtra("name", newChatName.toString())
                    startActivity(i)
                    finish()
                } catch (e: Exception) { /* model might not be available */ }
            }
        }

        messageCounter++
    }

    private fun pronounce(st: Boolean, message: String) {
        if ((st && isTTSInitialized && !silenceMode) || preferences!!.getNotSilence()) {
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
        if (preferences!!.getTtsEngine() == "google") {
            tts!!.speak(message, TextToSpeech.QUEUE_FLUSH, null, "")
        } else {
            speakScope = CoroutineScope(Dispatchers.Main)

            speakScope?.launch {
                progress?.setOnClickListener {
                    cancel()
                    restoreUIState()
                }

                try {
                    val rawAudio = ai!!.speech(
                        request = SpeechRequest(
                            model = ModelId("tts-1"),
                            input = message,
                            voice = com.aallam.openai.api.audio.Voice(preferences!!.getOpenAIVoice()),
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
                } catch (e: CancellationException) {
                    Toast.makeText(this@ChatActivity, "Cancelled", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun writeImageToCache(bytes: ByteArray, imageType: String = "png") {
        try {
            contentResolver.openFileDescriptor(Uri.fromFile(File(getExternalFilesDir("images")?.absolutePath + "/" + Hash.hash(java.util.Base64.getEncoder().encodeToString(bytes)) + "." + imageType)), "w")?.use {
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
                    model = ModelId("dall-e-${preferences!!.getDalleVersion()}"),
                    n = 1,
                    size = ImageSize(resolution)
                )
            )

            val url = URL(images?.get(0)?.url!!)

            val `is` = withContext(Dispatchers.IO) {
                url.openStream()
            }
            Thread {
                val bytes: ByteArray = org.apache.commons.io.IOUtils.toByteArray(`is`)

                writeImageToCache(bytes)

                val encoded = java.util.Base64.getEncoder().encodeToString(bytes)

                val file = Hash.hash(encoded)

                runOnUiThread {
                    putMessage("~file:$file", true)

                    chat?.setOnTouchListener { _, event -> run {
                        if (event.action == MotionEvent.ACTION_SCROLL || event.action == MotionEvent.ACTION_UP) {
                            chat?.transcriptMode = ListView.TRANSCRIPT_MODE_DISABLED
                            disableAutoScroll = true
                        }
                        return@setOnTouchListener false
                    }}

                    saveSettings()

                    btnMicro?.isEnabled = true
                    btnSend?.isEnabled = true
                    progress?.visibility = View.GONE

                    messageInput?.requestFocus()
                }
            }.start()
        } catch (e: CancellationException) {
            runOnUiThread {
                restoreUIState()
            }
        } catch (e: Exception) {
            if (preferences?.showChatErrors() == true) {
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
        } finally {
            runOnUiThread {
                restoreUIState()
            }
        }
    }

    private fun findLastUserMessage(): String {
        var lastUserMessage = ""

        for (i in messages.size - 1 downTo 0) {
            if (messages[i]["isBot"] == false) {
                lastUserMessage = messages[i]["message"].toString()
                break
            }
        }

        return lastUserMessage
    }

    private fun removeLastAssistantMessageIfAvailable() {
        if (messages.isNotEmpty() && messages.size - 1 > 0 && messages[messages.size - 1]["isBot"] == true) {
            messages.removeAt(messages.size - 1)
        }

        if (chatMessages.isNotEmpty() && chatMessages.size - 1 > 0 && chatMessages[chatMessages.size - 1].role == Role.Assistant) {
            chatMessages.removeAt(chatMessages.size - 1)
        }
    }

    override fun onRetryClick() {
        removeLastAssistantMessageIfAvailable()
        saveSettings()
        parseMessage(findLastUserMessage(), false)
    }

    private fun syncChatProjection() {
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
    }

    override fun onMessageEdited() {
        syncChatProjection()
    }

    override fun onMessageDeleted() {
        syncChatProjection()
    }
}
