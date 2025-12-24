/**************************************************************************
 * Copyright (c) 2023-2026 Dmytro Ostapenko. All rights reserved.
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
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Configuration.KEYBOARD_QWERTY
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.text.Editable
import android.text.TextWatcher
import android.transition.TransitionInflater
import android.util.Base64
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.WindowInsets
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.scale
import androidx.core.net.toUri
import androidx.core.util.Pair
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aallam.ktoken.Encoding
import com.aallam.ktoken.Tokenizer
import com.aallam.openai.api.audio.SpeechRequest
import com.aallam.openai.api.audio.TranscriptionRequest
import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionChunk
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.chat.ContentPart
import com.aallam.openai.api.chat.ImagePart
import com.aallam.openai.api.chat.TextPart
import com.aallam.openai.api.chat.ToolCall
import com.aallam.openai.api.chat.ToolChoice
import com.aallam.openai.api.chat.chatCompletionRequest
import com.aallam.openai.api.completion.CompletionRequest
import com.aallam.openai.api.completion.TextCompletion
import com.aallam.openai.api.core.Role
import com.aallam.openai.api.file.FileSource
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.image.ImageCreation
import com.aallam.openai.api.image.ImageSize
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.api.logging.Logger
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost
import com.aallam.openai.client.RetryStrategy
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.gson.Gson
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentifier
import com.openai.client.OpenAIClient
import com.openai.client.okhttp.OpenAIOkHttpClient
import com.openai.models.images.Image
import com.openai.models.images.ImageGenerateParams
import eightbitlab.com.blurview.BlurView
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.ApiEndpointPreferences
import org.teslasoft.assistant.preferences.ChatPreferences
import org.teslasoft.assistant.preferences.GlobalPreferences
import org.teslasoft.assistant.preferences.LogitBiasPreferences
import org.teslasoft.assistant.preferences.Preferences
import org.teslasoft.assistant.preferences.dto.ApiEndpointObject
import org.teslasoft.assistant.theme.ThemeManager
import org.teslasoft.assistant.ui.adapters.chat.ChatAdapter
import org.teslasoft.assistant.ui.fragments.dialogs.EditApiEndpointDialogFragment
import org.teslasoft.assistant.ui.fragments.dialogs.QuickSettingsBottomSheetDialogFragment
import org.teslasoft.assistant.ui.onboarding.WelcomeActivity
import org.teslasoft.assistant.ui.permission.CameraPermissionActivity
import org.teslasoft.assistant.ui.permission.MicrophonePermissionActivity
import org.teslasoft.assistant.util.Hash
import org.teslasoft.assistant.util.LocaleParser
import org.teslasoft.assistant.util.WindowInsetsUtil
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.util.EnumSet
import java.util.Locale
import java.util.Optional
import kotlin.time.Duration.Companion.seconds
import androidx.core.content.edit
import androidx.core.view.WindowInsetsCompat

class ChatActivity : FragmentActivity(), ChatAdapter.OnUpdateListener {

    // Init UI
    private var messageInput: EditText? = null
    private var btnSend: ImageButton? = null
    private var btnMicro: ImageButton? = null
    private var btnSettings: ImageButton? = null
    private var progress: CircularProgressIndicator? = null
    private var chat: RecyclerView? = null
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
    private var bulkContainer: ConstraintLayout? = null
    private var btnSelectAll: ImageButton? = null
    private var btnDeselectAll: ImageButton? = null
    private var btnDeleteSelected: ImageButton? = null
    private var btnCopySelected: ImageButton? = null
    private var btnShareSelected: ImageButton? = null
    private var selectedCount: TextView? = null
    private var expandableWindowRoot: CoordinatorLayout? = null
    private var blurSelectorView: BlurView? = null

    // Init chat
    private var messages: ArrayList<HashMap<String, Any>> = arrayListOf()
    private var messagesSelectionProjection: ArrayList<HashMap<String, Any>> = arrayListOf()
    private var messagesUsageProjection: ArrayList<HashMap<String, Any>> = arrayListOf()
    private var adapter: ChatAdapter? = null
    private var chatMessages: ArrayList<ChatMessage> = arrayListOf()
    private var chatId = ""
    private var chatName = ""
    private var languageIdentifier: LanguageIdentifier? = null

    // Init states
    private var isRecording = false
    private var keyboardMode = false
    private var isTTSInitialized = false
    private var silenceMode = false
    private var autoLangDetect = false
    private var cancelState = false
    private var disableAutoScroll = false
    private var imageIsSelected = false
    private var inCost: Float = 0.0f
    private var outCost: Float = 0.0f
    private var usageIn: Int = 0
    private var usageOut: Int = 0
    private var priceIn: Float = 0.0f
    private var priceOut: Float = 0.0f
    private var bulkSelectionMode: Boolean = false

    // init AI
    private var ai: OpenAI? = null
    private var openAIAI: OpenAI? = null
    private var key: String? = null
    private var openAIKey: String? = null
    private var model = ""
    private var endSeparator = ""
    private var prefix = ""
    private var apiEndpointPreferences: ApiEndpointPreferences? = null
    private var logitBiasPreferences: LogitBiasPreferences? = null
    private var apiEndpointObject: ApiEndpointObject? = null

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

    private var onSpeechResultsScope: CoroutineScope? = null
    private var whisperScope: CoroutineScope? = null
    private var processRecordingScope: CoroutineScope? = null
    private var setupScope: CoroutineScope? = null
    private var imageRequestScope: CoroutineScope? = null
    private var speakScope: CoroutineScope? = null
    private var generateGptImageJob: Job? = null

    private fun killAllProcesses() {
        onSpeechResultsScope?.coroutineContext?.cancel(CancellationException("Killed"))
        whisperScope?.coroutineContext?.cancel(CancellationException("Killed"))
        processRecordingScope?.coroutineContext?.cancel(CancellationException("Killed"))
        setupScope?.coroutineContext?.cancel(CancellationException("Killed"))
        imageRequestScope?.coroutineContext?.cancel(CancellationException("Killed"))
        speakScope?.coroutineContext?.cancel(CancellationException("Killed"))
        generateGptImageJob?.cancel(CancellationException("Killed"))
        generateGptImageJob = null
    }

    private fun restoreUIState() {
        runOnUiThread {
            progress?.visibility = View.GONE
            btnMicro?.isEnabled = true
            btnSend?.isEnabled = true
            isRecording = false
            btnMicro?.setImageResource(R.drawable.ic_microphone)
            cancelState = false
        }
    }

    private suspend fun tokenizeArray() {
        messagesUsageProjection = arrayListOf()
        messagesUsageProjection.clear()

        if (chatMessages == null) chatMessages = arrayListOf()

        for (m in chatMessages) {
            val tokenizer = Tokenizer.of(encoding = Encoding.CL100K_BASE)
            val tokens = tokenizer.encode(m.content.toString()).size

            messagesUsageProjection.add(
                hashMapOf(
                    "isBot" to (m.role == Role.Assistant),
                    "tokens" to if (m.content.toString().trim().startsWith("~file:")) 0 else tokens
                )
            )
        }
    }

    private fun calculateCost() {
        CoroutineScope(Dispatchers.Main).launch {
            tokenizeArray()

            usageIn = 0
            usageOut = 0
            inCost = 0.0f
            outCost = 0.0f

            var i = messagesUsageProjection.size - 1

            while (i > 0) {
                var j = 0
                var c = 0

                while (j < i) {
                    c += messagesUsageProjection[j]["tokens"] as Int
                    j++
                }

                usageIn += c
                i--
            }

            for (m in messagesUsageProjection) {
                val msgUsage = if (m["isBot"] == true) m["tokens"] as Int else 0

                usageOut += msgUsage
            }

            inCost = usageIn * priceIn
            outCost = usageOut * priceOut
        }
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
                            } catch (_: CancellationException) {
                                restoreUIState()
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

    @Suppress("deprecation")
    private fun preloadAmoled() {
        if (isDarkThemeEnabled() && GlobalPreferences.getPreferences(this).getAmoledPitchBlack()) {
            threadLoader?.backgroundTintList = ColorStateList.valueOf(ResourcesCompat.getColor(resources, R.color.amoled_accent_50, theme))

            if (Build.VERSION.SDK_INT < 30) {
                window.statusBarColor = ResourcesCompat.getColor(resources, R.color.amoled_accent_50, theme)
                window.navigationBarColor = ResourcesCompat.getColor(resources, R.color.amoled_accent_50, theme)
            }
        } else {
            threadLoader?.backgroundTintList = ColorStateList.valueOf(SurfaceColors.SURFACE_1.getColor(this))

            if (Build.VERSION.SDK_INT < 30) {
                window.statusBarColor = SurfaceColors.SURFACE_1.getColor(this)
                window.navigationBarColor = SurfaceColors.SURFACE_1.getColor(this)
            }
        }
    }

    fun resizeBitmapToMaxHeight(bitmap: Bitmap, maxHeight: Int = 100): Bitmap {
        val originalHeight = bitmap.height
        val originalWidth = bitmap.width

        if (originalHeight <= maxHeight) {
            // Return a copy of the original bitmap if already smaller than or equal to maxHeight
            return bitmap.copy(bitmap.config ?: return bitmap, true)
        }

        // Calculate the new dimensions while keeping the aspect ratio
        val aspectRatio = originalWidth.toFloat() / originalHeight.toFloat()
        val newWidth = (maxHeight * aspectRatio).toInt()

        // Create the scaled bitmap
        return bitmap.scale(newWidth, maxHeight)
    }


    private var cameraIntentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "tmp.jpg")
            val uri = FileProvider.getUriForFile(this, "org.teslasoft.assistant.fileprovider", imageFile)

            bitmap = readFile(uri)

            if (bitmap != null) {
                attachedImage?.visibility = View.VISIBLE

                val bitmapResizedForPreview = resizeBitmapToMaxHeight(bitmap!!, 100)

                selectedImage?.setImageBitmap(roundCorners(bitmapResizedForPreview))
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
            if (result.resultCode == RESULT_OK) {
                val intent = Intent().setAction(MediaStore.ACTION_IMAGE_CAPTURE)
                intent.putExtra("android.intent.extra.quickCapture", true)
                val externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                val imageFile = File(externalFilesDir, "tmp.jpg")
                intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this, "org.teslasoft.assistant.fileprovider", imageFile))
                cameraIntentLauncher.launch(intent)
            }
        }
    }

    @Suppress("deprecation")
    private fun reloadAmoled() {
        ThemeManager.getThemeManager().applyTheme(this, isDarkThemeEnabled() && GlobalPreferences.getPreferences(this).getAmoledPitchBlack())
        if (isDarkThemeEnabled() && GlobalPreferences.getPreferences(this).getAmoledPitchBlack()) {
            if (Build.VERSION.SDK_INT < 30) {
                window.statusBarColor = ResourcesCompat.getColor(resources, R.color.amoled_accent_100, theme)
                window.navigationBarColor = ResourcesCompat.getColor(resources, R.color.amoled_accent_100, theme)
            }
            progress?.setBackgroundResource(R.drawable.assistant_clear_amoled)
            keyboardFrame?.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.amoled_accent_100, theme))
            actionBar?.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.amoled_accent_100, theme))
            activityTitle?.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.amoled_accent_100, theme))
            messageInput?.setHintTextColor(ResourcesCompat.getColor(resources, R.color.amoled_accent_900, theme))
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
            if (Build.VERSION.SDK_INT < 30) {
                window.statusBarColor = SurfaceColors.SURFACE_4.getColor(this)
                window.navigationBarColor = SurfaceColors.SURFACE_2.getColor(this)
            }
            progress?.setBackgroundResource(R.drawable.assistant_clear_v2)
            keyboardFrame?.setBackgroundColor(SurfaceColors.SURFACE_2.getColor(this))
            actionBar?.setBackgroundColor(SurfaceColors.SURFACE_4.getColor(this))
            activityTitle?.setBackgroundColor(SurfaceColors.SURFACE_4.getColor(this))
            messageInput?.setHintTextColor(ResourcesCompat.getColor(resources, R.color.accent_900, theme))
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
            if (result.resultCode == RESULT_OK) {
                startRecognition()
            }
        }
    }

    private val permissionResultLauncherV2 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        run {
            if (result.resultCode == RESULT_OK) {
                startWhisper()
            }
        }
    }

    private val settingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { recreate() }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= 30) {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
                navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
            )
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val transition = TransitionInflater.from(this).inflateTransition(android.R.transition.move).apply {
            interpolator = LinearOutSlowInInterpolator()
            duration = 300
        }

        val transition2 = TransitionInflater.from(this).inflateTransition(android.R.transition.move).apply {
            interpolator = FastOutLinearInInterpolator()
            duration = 200
        }

        // Set the transition as the shared element enter transition
        window.sharedElementEnterTransition = transition
        window.sharedElementExitTransition = transition2

        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= 33) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) {
                if (bulkSelectionMode) {
                    deselectAll()
                } else {
                    finishActivity()
                }
            }
        } else {
            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (bulkSelectionMode) {
                        deselectAll()
                    } else {
                        finishActivity()
                    }
                }
            })
        }

        setContentView(R.layout.activity_chat)

        preloadAmoled()
        reloadAmoled()

        mediaPlayer = MediaPlayer()
        threadLoader = findViewById(R.id.thread_loader)
        threadLoader?.visibility = View.VISIBLE

        Thread {
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)

            runOnUiThread {
                val chatActivityTitle: TextView = findViewById(R.id.chat_activity_title)
                val keyboardInput: LinearLayout = findViewById(R.id.keyboard_input)

                chatActivityTitle.setBackgroundColor(SurfaceColors.SURFACE_4.getColor(this))
                keyboardInput.setBackgroundColor(SurfaceColors.SURFACE_5.getColor(this))

                initChatId()
                initSettings()

                if (savedInstanceState != null) {
                    adjustPaddings()
                    onRestoredState(savedInstanceState)
                }
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
        openAIKey = apiEndpointPreferences?.findOpenAIKeyIfAvailable(this)

        endSeparator = preferences!!.getEndSeparator()
        prefix = preferences!!.getPrefix()

        loadResolution()

        if (key == null) {
            startActivity(Intent(this, WelcomeActivity::class.java).setAction(Intent.ACTION_VIEW))
            finishActivity()
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

            updateMessagesSelectionProjection()

            calculateCost()

            adapter = ChatAdapter(messages, messagesSelectionProjection,this, preferences!!, false, chatId)
            adapter?.setOnUpdateListener(this)

            initUI()
            reloadAmoled()
            initSpeechListener()
            initTTS()
            initLogic()
            initAI()
        }
    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility", "NotifyDataSetChanged")
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
        bulkContainer = findViewById(R.id.bulk_container)
        btnSelectAll = findViewById(R.id.btn_select_all)
        btnDeselectAll = findViewById(R.id.btn_deselect_all)
        btnDeleteSelected = findViewById(R.id.btn_delete_selected)
        btnCopySelected = findViewById(R.id.btn_copy_selected)
        btnShareSelected = findViewById(R.id.btn_share_selected)
        selectedCount = findViewById(R.id.text_selected_count)
        expandableWindowRoot = findViewById(R.id.expandable_window_root)
        blurSelectorView = findViewById(R.id.attach_bg)

        val radius = 16f
        val decorView = window.decorView
        val rootView = decorView.findViewById<ViewGroup>(android.R.id.content)
        val windowBackground = decorView.background
        blurSelectorView?.setupWith(rootView)
            ?.setFrameClearDrawable(windowBackground)
            ?.setBlurRadius(radius)

        blurSelectorView?.outlineProvider = ViewOutlineProvider.BACKGROUND
        blurSelectorView?.setClipToOutline(true)

        if (isDarkThemeEnabled() && GlobalPreferences.getPreferences(this).getAmoledPitchBlack()) {
            expandableWindowRoot?.backgroundTintList = ColorStateList.valueOf(getColor(R.color.amoled_window_background))
        } else {
            expandableWindowRoot?.backgroundTintList = ColorStateList.valueOf(SurfaceColors.SURFACE_1.getColor(this))
        }

        bulkContainer?.visibility = View.GONE

        chat?.itemAnimator = null

        visionActions?.visibility = View.GONE

        attachedImage?.visibility = View.GONE

        btnExport?.setImageResource(R.drawable.ic_upload)
        btnBack?.setImageResource(R.drawable.ic_back)

        activityTitle?.text = if (chatName.trim().contains("_autoname_")) "Untitled chat" else chatName

        activityTitle?.isSelected = true

        progress?.visibility = View.GONE

        btnMicro?.setImageResource(R.drawable.ic_microphone)
        btnSettings?.setImageResource(R.drawable.ic_settings)

        btnSelectAll?.setOnClickListener {
            selectAll()
        }

        btnDeselectAll?.setOnClickListener {
            deselectAll()
        }

        btnDeleteSelected?.setOnClickListener {
            deleteSelectedMessages()
        }

        btnCopySelected?.setOnClickListener {
            copySelectedMessages()
        }

        btnShareSelected?.setOnClickListener {
            shareSelectedMessages()
        }

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
            finishActivity()
        }

        activityTitle?.setOnClickListener {
            val quickSettingsBottomSheetDialogFragment = QuickSettingsBottomSheetDialogFragment
                .newInstance(
                    chatId,
                    usageIn,
                    usageOut,
                    priceIn,
                    priceOut
                )
            quickSettingsBottomSheetDialogFragment.setOnUpdateListener(object : QuickSettingsBottomSheetDialogFragment.OnUpdateListener {
                override fun onUpdate() {
                    /* for future */
                }

                override fun onForceUpdate() {
                    startActivity(Intent(this@ChatActivity, ChatActivity::class.java).putExtra("chatId", chatId).putExtra("name", chatName).setAction(Intent.ACTION_VIEW))
                    finishActivity()
                }
            })
            quickSettingsBottomSheetDialogFragment.show(supportFragmentManager, "QuickSettingsBottomSheetDialogFragment")
        }

        val linearLayoutManager = LinearLayoutManager(this)
        // linearLayoutManager.stackFromEnd = true

        chat?.setLayoutManager(linearLayoutManager)

        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(chat)

        chat?.adapter = adapter

        adapter?.notifyDataSetChanged()

        chat?.post {
            chat?.scrollToPosition(adapter?.itemCount!! - 1)
        }

        chat?.setOnTouchListener { _, event -> run {
            if (event.action == MotionEvent.ACTION_SCROLL || event.action == MotionEvent.ACTION_UP) {
                // chat?.transcriptMode = ListView.TRANSCRIPT_MODE_DISABLED
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
                        reloadAmoled()
                    }
                }

                override fun onAnimationRepeat(animation: Animation) { /* UNUSED */ }
            })
        }, 50)
    }

    private val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
            val position = viewHolder.bindingAdapterPosition

            viewHolder.itemView.post {
                adapter?.notifyItemChanged(position)
                adapter?.notifyDataSetChanged() // ??? ...

                if (viewHolder is ChatAdapter.ViewHolder) {
                    viewHolder.resetView()
                }

                if (swipeDir == ItemTouchHelper.LEFT && !bulkSelectionMode) {
                    MaterialAlertDialogBuilder(this@ChatActivity, R.style.App_MaterialAlertDialog)
                        .setTitle(R.string.label_confirm_deletion)
                        .setMessage(R.string.msg_confirm_deletion_chat)
                        .setPositiveButton(R.string.btn_delete) { _, _ -> run {
                            adapter?.onDelete(position)
                        }}
                        .setNegativeButton(R.string.btn_cancel) { _, _ -> }
                        .show()
                }
            }
        }

        override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                                 dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {

            val iconDRight = if (maxX(dX.toInt() / 5) == dpToPx(-32)) {
                ResourcesCompat.getDrawable(resources, R.drawable.ic_delete_action_active, theme)!!
            } else {
                ResourcesCompat.getDrawable(resources, R.drawable.ic_delete_action, theme)!!
            }
            val itemView = viewHolder.itemView
            val background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(ResourcesCompat.getColor(resources, R.color.transparent, theme))
                cornerRadius = dpToPx(128).toFloat()
            }

            if (dX < 0) { // Swiping to the left
                val iconMargin = 48
                val iconTop = itemView.top + (itemView.height - iconDRight.intrinsicHeight) / 2
                val iconBottom = iconTop + iconDRight.intrinsicHeight
                val iconLeft = itemView.right - iconMargin - iconDRight.intrinsicWidth
                val iconRight = itemView.right - iconMargin
                iconDRight.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                background.setColor(ResourcesCompat.getColor(resources, R.color.delete_tint, theme))
                if (maxX(dX.toInt() / 5) == dpToPx(-32)) {
                    background.setColor(ResourcesCompat.getColor(resources, R.color.delete_tint_active, theme))
                }

                background.setBounds(iconLeft + maxX(dX.toInt() / 5), iconTop + maxX(dX.toInt() / 5), iconRight - maxX(dX.toInt() / 5), iconBottom - maxX(dX.toInt() / 5))
                background.draw(c)
                iconDRight.draw(c)
            }

            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && !isCurrentlyActive) {
                getDefaultUIUtil().clearView(viewHolder.itemView)
            }
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources?.displayMetrics?.density!!).toInt()
    }

    private fun maxX(x: Int) : Int {
        if (x < dpToPx(-32)) return dpToPx(-32)
        else if (x < dpToPx(32)) return x
        return dpToPx(32)
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

    private fun roundCorners(bitmap: Bitmap): Bitmap {
        // Create a bitmap with the same size as the original.
        val output = createBitmap(bitmap.width, bitmap.height)

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
        canvas.drawRoundRect(rectF, 16f, 16f, paint)

        // Change the paint mode to draw the original bitmap on top.
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        // Draw the original bitmap.
        canvas.drawBitmap(bitmap, rect, rect, paint)

        return output
    }

    private val fileIntentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        run {
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.also { uri ->
                    bitmap = readFile(uri)

                    if (bitmap != null) {
                        attachedImage?.visibility = View.VISIBLE

                        val resizedBitmap = resizeBitmapToMaxHeight(bitmap!!, 100)

                        selectedImage?.setImageBitmap(roundCorners(resizedBitmap))
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
            openFile("/storage/emulated/0/image.png".toUri())
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
            when (event.action) {
                KeyEvent.ACTION_DOWN if keyCode == KeyEvent.KEYCODE_ENTER && event.isShiftPressed && isHardKB() && preferences!!.getDesktopMode() -> {
                    (v as EditText).append("\n")
                    return@run true
                }
                KeyEvent.ACTION_DOWN if keyCode == KeyEvent.KEYCODE_ENTER && isHardKB() && preferences!!.getDesktopMode() -> {
                    parseMessage((v as EditText).text.toString())
                    return@run true
                }
                KeyEvent.ACTION_DOWN if ((keyCode == KeyEvent.KEYCODE_ESCAPE && event.isShiftPressed) || keyCode == KeyEvent.KEYCODE_BACK) && preferences!!.getDesktopMode() -> {
                    finishActivity()
                    return@run true
                }
                else -> return@run false
            }
        }}

        if (preferences!!.getDesktopMode()) {
            messageInput?.requestFocus()
        }

        btnSettings?.setOnClickListener {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                Pair.create(btnSettings, ViewCompat.getTransitionName(btnSettings!!))
            )
            settingsLauncher.launch(
                Intent(this, SettingsActivity::class.java).setAction(Intent.ACTION_VIEW).putExtra("chatId", chatId),
                options
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
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, (Environment.getExternalStorageDirectory().path + "/SpeakGPT/$chatId.json").toUri())
            }
            fileSaveIntentLauncher.launch(intent)
        }
    }

    private fun isHardKB(): Boolean {
        return resources.configuration.keyboard == KEYBOARD_QWERTY
    }

    private val fileSaveIntentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        run {
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.also { uri ->
                    writeToFile(uri)
                }
            }
        }
    }

    private fun writeToFile(uri: Uri) {
        try {
            contentResolver.openFileDescriptor(uri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use { stream ->
                    stream.write(
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
        if (openAIKey == null) {
            openAIMissing("whisper", "")
        } else if (Build.VERSION.SDK_INT >= 31) {
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
                    } catch (_: IOException) {
                        btnMicro?.setImageResource(R.drawable.ic_microphone)
                        isRecording = false
                        MaterialAlertDialogBuilder(
                            this@ChatActivity,
                            R.style.App_MaterialAlertDialog
                        )
                            .setTitle(R.string.label_audio_error)
                            .setMessage(R.string.msg_audio_error)
                            .setPositiveButton(R.string.btn_close) { _, _ -> }
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
                    } catch (_: IOException) {
                        btnMicro?.setImageResource(R.drawable.ic_microphone)
                        isRecording = false
                        MaterialAlertDialogBuilder(
                            this@ChatActivity,
                            R.style.App_MaterialAlertDialog
                        )
                            .setTitle(R.string.label_audio_error)
                            .setMessage(R.string.msg_audio_error)
                            .setPositiveButton(R.string.btn_close) { _, _ -> }
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
                } catch (_: CancellationException) {
                    restoreUIState()
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
                    path = Path("${externalCacheDir?.absolutePath}/tmp.m4a"),
                    fileSystem = SystemFileSystem
                ),
                model = ModelId("whisper-1"),
            )
            val transcription = openAIAI?.transcription(transcriptionRequest)!!.text

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
                        } catch (_: CancellationException) {
                            restoreUIState()
                        }
                    }
                } else {
                    restoreUIState()
                    messageInput?.setText(transcription)
                }
            }
        } catch (_: Exception) {
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
            } catch (_: java.lang.Exception) {/* unused */}
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
            } catch (_: java.lang.Exception) {/* unused */}
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
            finishActivity()
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
            val configOpenAI = OpenAIConfig(
                token = openAIKey.toString(),
                logging = LoggingConfig(LogLevel.None, Logger.Simple),
                timeout = Timeout(socket = 30.seconds),
                organization = null,
                headers = emptyMap(),
                host = OpenAIHost("https://api.openai.com/v1/"),
                proxy = null,
                retry = RetryStrategy()
            )
            openAIAI = OpenAI(configOpenAI)
            loadModel()
            setup()
        }
    }

    private fun initChatId() {
        val extras: Bundle? = intent.extras

        if (extras != null) {
            chatId = extras.getString("chatId", "")
            chatName = extras.getString("name", "")

            this.title = chatName
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
                        saveSettings()
                        syncChatProjection()
                        calculateCost()
                    }

                    try {
                        generateResponse(prompt, false)
                    } catch (_: CancellationException) {
                        restoreUIState()
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

    // Init image resolutions
    private fun loadResolution() {
        resolution = preferences!!.getResolution()
    }

    /** SYSTEM INITIALIZATION END **/

    private fun saveSettings() {
        val chat = getSharedPreferences("chat_$chatId", MODE_PRIVATE)
        chat.edit {
            val gson = Gson()
            val json: String = gson.toJson(messages)

            putString("chat", json)
        }
    }

    private fun parseMessage(message: String, shouldAdd: Boolean = true) {
        // Put timestamp to chat to sort chats by last message
        ChatPreferences.getChatPreferences().putTimestampToChatById(this, chatId)
        try {
            if (mediaPlayer!!.isPlaying) {
                mediaPlayer!!.stop()
                mediaPlayer!!.reset()
            }
            tts!!.stop()
        } catch (_: java.lang.Exception) {/* unused */}
        if (message != "") {
            messageInput?.setText("")

            keyboardMode = false

            val m = prefix + message + endSeparator

            if (imageIsSelected) {
                val bytes = Base64.decode(baseImageString!!.split(",")[1], Base64.DEFAULT)
                writeImageToCache(bytes, selectedImageType!!)

                val encoded = java.util.Base64.getEncoder().encodeToString(bytes)

                val file = Hash.hash(encoded)

                if (shouldAdd) {
                    putMessage(m, false, file, selectedImageType!!)
                } else {
                    messages[messages.size - 1]["image"] = file
                    messages[messages.size - 1]["imageType"] = selectedImageType!!
                    messages[messages.size - 1]["message"] = m
                }
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

                if (openAIKey == null) {
                    openAIMissing("dalle", x)
                } else {
                    sendImageRequest(x)
                }
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
                    syncChatProjection()
                }

                CoroutineScope(Dispatchers.Main).launch {
                    progress?.setOnClickListener {
                        cancel()
                        restoreUIState()
                        saveSettings()
                        syncChatProjection()
                        calculateCost()
                    }

                    try {
                        generateResponse(m, false)
                    } catch (_: CancellationException) {
                        restoreUIState()
                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun sendImageRequest(str: String) {
        imageRequestScope = CoroutineScope(Dispatchers.Main)
        imageRequestScope?.launch {
            runOnUiThread {
                btnMicro?.isEnabled = false
                btnSend?.isEnabled = false
                progress?.visibility = View.VISIBLE
            }

            progress?.setOnClickListener {
                cancel()
                restoreUIState()
                saveSettings()
                syncChatProjection()
                calculateCost()
            }

            try {
                generateImageR(str)
            } catch (_: CancellationException) {
                restoreUIState()
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
        adapter?.notifyItemInserted(messages.size - 1)

        updateMessagesSelectionProjection()

        scroll(true)
    }

    private fun scroll(mode: Boolean) {
        if (!disableAutoScroll) {
            val itemCount = adapter?.itemCount ?: 0

            if (mode) {
                chat?.post {
                    if (itemCount > 0) {
                        chat?.scrollToPosition(itemCount - 1)

                        scrollX(itemCount)
                    }
                }
            } else {
                scrollX(itemCount)
            }
        }
    }

    private fun scrollX(itemCount: Int) {
        chat?.post {
            val lastView = chat?.layoutManager?.findViewByPosition(itemCount - 1)
            lastView?.let {
                val scrollDistance = it.bottom - (chat?.height ?: 0)
                if (scrollDistance > 0) {
                    chat?.scrollBy(0, scrollDistance)
                }
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
        intent.data = "https://www.google.com/search?q=$q".toUri()
        startActivity(intent)
    }

    @Suppress("deprecation")
    private suspend fun generateResponse(request: String, shouldPronounce: Boolean) {
        disableAutoScroll = false
        try {
            var response = ""

            if (imageIsSelected) {
                imageIsSelected = false

                attachedImage?.visibility = View.GONE

                putMessage("", true)

                val reqList: ArrayList<ContentPart> = arrayListOf()
                reqList.add(TextPart(request))
                reqList.add(ImagePart(baseImageString!!))
                val chatCompletionRequest = if (preferences?.getLogitBiasesConfigId() == null || preferences?.getLogitBiasesConfigId() == "null" || preferences?.getLogitBiasesConfigId() == "") {
                    ChatCompletionRequest(
                        model = ModelId("gpt-4o"),
                        temperature = if (preferences!!.getTemperature().toDouble() == 0.7) null else preferences!!.getTemperature().toDouble(),
                        topP = if (preferences!!.getTopP().toDouble() == 1.0) null else preferences!!.getTopP().toDouble(),
                        frequencyPenalty = if (preferences!!.getFrequencyPenalty().toDouble() == 0.0) null else preferences!!.getFrequencyPenalty().toDouble(),
                        presencePenalty = if (preferences!!.getPresencePenalty().toDouble() == 0.0) null else preferences!!.getPresencePenalty().toDouble(),
                        logitBias = logitBiasPreferences?.getLogitBiasesMap(),
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
                } else {
                    ChatCompletionRequest(
                        model = ModelId("gpt-4o"),
                        temperature = if (preferences!!.getTemperature().toDouble() == 0.7) null else preferences!!.getTemperature().toDouble(),
                        topP = if (preferences!!.getTopP().toDouble() == 1.0) null else preferences!!.getTopP().toDouble(),
                        frequencyPenalty = if (preferences!!.getFrequencyPenalty().toDouble() == 0.0) null else preferences!!.getFrequencyPenalty().toDouble(),
                        presencePenalty = if (preferences!!.getPresencePenalty().toDouble() == 0.0) null else preferences!!.getPresencePenalty().toDouble(),
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
                }

                val completions: Flow<ChatCompletionChunk> = ai!!.chatCompletions(chatCompletionRequest)

                scroll(true)

                completions.collect { v ->
                    run {
                        if (!currentCoroutineContext().isActive) throw CancellationException()
                        else if (v.choices[0].delta != null && v.choices[0].delta?.content != null && v.choices[0].delta?.content.toString() != "null") {
                            response += v.choices[0].delta?.content
                            if (response != "null") {
                                messages[messages.size - 1]["message"] = response
                                if (messages.size > 2) {
                                    adapter?.notifyItemRangeChanged(messages.size - 3, messages.size - 1)
                                } else {
                                    adapter?.notifyItemChanged(messages.size - 1)
                                }
                                scroll(false)
                                saveSettings()
                            }
                        }
                    }
                }

                messages[messages.size - 1]["message"] = "${response}\n"

                if (messages.size > 2) {
                    adapter?.notifyItemRangeChanged(messages.size - 3, messages.size - 1)
                } else {
                    adapter?.notifyItemChanged(messages.size - 1)
                }

                syncChatProjection()

                pronounce(shouldPronounce, response)

                saveSettings()
                calculateCost()

                btnMicro?.isEnabled = true
                btnSend?.isEnabled = true
                progress?.visibility = View.GONE
                messageInput?.requestFocus()
            } else if (model.contains(":ft") || model.contains("ft:")) {
                putMessage("", true)
                val completionRequest = if (preferences?.getLogitBiasesConfigId() == null || preferences?.getLogitBiasesConfigId() == "null" || preferences?.getLogitBiasesConfigId() == "") {
                    CompletionRequest(
                        model = ModelId(model),
                        temperature = if (model.contains("gpt-5") || model.contains("o1") || model.contains("o3")) 1.0 else if (preferences!!.getTemperature().toDouble() == 0.7) null else preferences!!.getTemperature().toDouble(),
                        topP = if (preferences!!.getTopP().toDouble() == 1.0) null else preferences!!.getTopP().toDouble(),
                        frequencyPenalty = if (preferences!!.getFrequencyPenalty().toDouble() == 0.0) null else preferences!!.getFrequencyPenalty().toDouble(),
                        presencePenalty = if (preferences!!.getPresencePenalty().toDouble() == 0.0) null else preferences!!.getPresencePenalty().toDouble(),
                        prompt = request,
                        logitBias = if (model.contains("gpt-5") || model.contains("o1") || model.contains("o3")) null else logitBiasPreferences?.getLogitBiasesMap(),
                        echo = false
                    )
                } else {
                    CompletionRequest(
                        model = ModelId(model),
                        temperature = if (model.contains("gpt-5") || model.contains("o1") || model.contains("o3")) 1.0 else if (preferences!!.getTemperature().toDouble() == 0.7) null else preferences!!.getTemperature().toDouble(),
                        topP = if (preferences!!.getTopP().toDouble() == 1.0) null else preferences!!.getTopP().toDouble(),
                        frequencyPenalty = if (preferences!!.getFrequencyPenalty().toDouble() == 0.0) null else preferences!!.getFrequencyPenalty().toDouble(),
                        presencePenalty = if (preferences!!.getPresencePenalty().toDouble() == 0.0) null else preferences!!.getPresencePenalty().toDouble(),
                        prompt = request,
                        echo = false
                    )
                }

                val completions: Flow<TextCompletion> = ai!!.completions(completionRequest)

                completions.collect { v ->
                    run {
                        if (!currentCoroutineContext().isActive) throw CancellationException()
                        else if (v.choices[0] != null && v.choices[0].text != null && v.choices[0].text.toString() != "null") {
                            response += v.choices[0].text
                            messages[messages.size - 1]["message"] = response
                            if (messages.size > 2) {
                                adapter?.notifyItemRangeChanged(messages.size - 3, messages.size - 1)
                            } else {
                                adapter?.notifyItemChanged(messages.size - 1)
                            }
                            saveSettings()
                        }
                    }
                }

                messages[messages.size - 1]["message"] = "$response\n"
                if (messages.size > 2) {
                    adapter?.notifyItemRangeChanged(messages.size - 3, messages.size - 1)
                } else {
                    adapter?.notifyItemChanged(messages.size - 1)
                }

                syncChatProjection()

                saveSettings()
                calculateCost()

                pronounce(shouldPronounce, response)

                btnMicro?.isEnabled = true
                btnSend?.isEnabled = true
                progress?.visibility = View.GONE
                messageInput?.requestFocus()
            } else {
                val functionCallingEnabled: Boolean = preferences!!.getFunctionCalling()

                if (functionCallingEnabled && openAIKey != null) {
                    val cm = mutableListOf(
                        ChatMessage(
                            role = ChatRole.User,
                            content = request
                        )
                    )

                    val functionRequest = chatCompletionRequest {
                        model = ModelId("gpt-4o")
                        messages = cm

                        tools {
                            function(
                                name = "generateImage",
                                description = "Generate an image based on the entered prompt"
                            ) {
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

                            function(
                                name = "searchAtInternet",
                                description = "Search the Internet",
                            ) {
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
                        }

                        toolChoice = ToolChoice.Auto
                    }

                    val response1 = openAIAI?.chatCompletion(functionRequest)

                    val message = response1?.choices?.first()?.message

                    if (message?.toolCalls != null) {
                        val toolsCalls = message.toolCalls!!

                        if (toolsCalls.isEmpty()) {
                            regularGPTResponse(shouldPronounce)
                        } else {
                            for (toolCall in toolsCalls) {
                                require(toolCall is ToolCall.Function) { "Tool call is not a function" }
                                toolCall.execute()
                            }

                            // Put timestamp to chat to sort chats by last message
                            ChatPreferences.getChatPreferences().putTimestampToChatById(this, chatId)
                        }
                    } else {
                        regularGPTResponse(shouldPronounce)
                    }
                } else if (functionCallingEnabled) {
                    putMessage("Function calling requires OpenAI endpoint which is missing on your device. Please go to the settings and add OpenAI endpoint or disable Function Calling. OpenAI base url (host) is: https://api.openai.com/v1/ (don't forget to add slash at the end otherwise you will receive an error).", true)
                    saveSettings()
                    restoreUIState()
                    MaterialAlertDialogBuilder(this, R.style.App_MaterialAlertDialog)
                        .setTitle("Unsupported feature")
                        .setMessage("Function calling feature is unavailable because it requires OpenAI endpoint. Would you like to disable this feature?")
                        .setPositiveButton("Disable") { _, _ -> run {
                            preferences?.setFunctionCalling(false)
                        }}
                        .setNegativeButton("Cancel") { _, _ -> }
                        .show()
                } else {
                    regularGPTResponse(shouldPronounce)
                }
            }
        } catch (_: CancellationException) {
            calculateCost()
            runOnUiThread {
                restoreUIState()
            }
        } catch (e: Exception) {
            val response = when {
                e.stackTraceToString().contains("invalid model") -> {
                    getString(R.string.prompt_no_model_provided)
                }
                e.stackTraceToString().contains("does not exist") -> {
                    String.format(getString(R.string.prompt_model_not_available), model)
                }
                e.stackTraceToString().contains("Connect timeout has expired") || e.stackTraceToString().contains("SocketTimeoutException") -> {
                    getString(R.string.prompt_timed_out)
                }
                e.stackTraceToString().contains("This model's maximum") -> {
                    getString(R.string.prompt_max_tokens_error)
                }
                e.stackTraceToString().contains("No address associated with hostname") -> {
                    getString(R.string.prompt_offline)
                }
                e.stackTraceToString().contains("Incorrect API key") -> {
                    getString(R.string.prompt_key_invalid)
                }
                e.stackTraceToString().contains("you must provide a model") -> {
                    getString(R.string.prompt_no_model)
                }
                e.stackTraceToString().contains("Software caused connection abort") -> {
                    getString(R.string.prompt_error_unknown)
                }
                e.stackTraceToString().contains("You exceeded your current quota") -> {
                    getString(R.string.prompt_quota_reached)
                }
                else -> {
                    e.stackTraceToString() + "\n\n" + e.message
                }
            }

            if (messages[messages.size - 1]["isBot"] == false) {
                putMessage("", true)
            }

            if (preferences?.showChatErrors() == true) {
                messages[messages.size - 1]["message"] = "${messages[messages.size - 1]["message"]}\n\n${getString(R.string.prompt_show_error)}\n\n$response"
                if (messages.size > 2) {
                    adapter?.notifyItemRangeChanged(messages.size - 3, messages.size - 1)
                } else {
                    adapter?.notifyItemChanged(messages.size - 1)
                }
            }

            saveSettings()
            calculateCost()

            runOnUiThread {
                btnMicro?.isEnabled = true
                btnSend?.isEnabled = true
                progress?.visibility = View.GONE
                messageInput?.requestFocus()
            }
        } finally {
            calculateCost()
            runOnUiThread {
                restoreUIState()
            }
        }
    }

    private val availableFunctions = mapOf("generateImage" to ::generateImage, "searchAtInternet" to ::searchAtInternet)

    private fun ToolCall.Function.execute() {
        val functionToCall = availableFunctions[function.name] ?: error("Function ${function.name} not found")
        val functionArgs = function.argumentsAsJson()
        functionToCall(functionArgs)
    }

    private fun generateImage(args: JsonObject) {
        val prompt = args.getValue("prompt").jsonPrimitive.content

        runOnUiThread {
            btnMicro?.isEnabled = false
            btnSend?.isEnabled = false
            progress?.visibility = View.VISIBLE
        }

        CoroutineScope(Dispatchers.Main).launch {
            generateImages(prompt)
        }
    }

    private fun searchAtInternet(args: JsonObject) {
        val prompt = args.getValue("prompt").jsonPrimitive.content

        runOnUiThread {
            btnMicro?.isEnabled = false
            btnSend?.isEnabled = false
            progress?.visibility = View.VISIBLE
        }

        CoroutineScope(Dispatchers.Main).launch {
            searchInternet(prompt)
        }
    }

    private suspend fun regularGPTResponse(shouldPronounce: Boolean) {
        disableAutoScroll = false

        var response = ""
        putMessage("", true)

        val msgs: ArrayList<ChatMessage> = arrayListOf()

        val systemMessage = preferences!!.getSystemMessage()
        if (systemMessage != "") {
            msgs.add(
                ChatMessage(
                    role = ChatRole.System,
                    content = systemMessage
                )
            )
        }

        msgs.addAll(chatMessages)

        val chatCompletionRequest = if (preferences?.getLogitBiasesConfigId() == null || preferences?.getLogitBiasesConfigId() == "null" || preferences?.getLogitBiasesConfigId() == "") {
            ChatCompletionRequest(
                model = ModelId(model),
                temperature = if (model.contains("gpt-5") || model.contains("o1") || model.contains("o3")) 1.0 else if (preferences!!.getTemperature().toDouble() == 0.7) null else preferences!!.getTemperature().toDouble(),
                topP = if (preferences!!.getTopP().toDouble() == 1.0) null else preferences!!.getTopP().toDouble(),
                frequencyPenalty = if (preferences!!.getFrequencyPenalty().toDouble() == 0.0) null else preferences!!.getFrequencyPenalty().toDouble(),
                presencePenalty = if (preferences!!.getPresencePenalty().toDouble() == 0.0) null else preferences!!.getPresencePenalty().toDouble(),
                seed = if (preferences!!.getSeed() != "") preferences!!.getSeed().toInt() else null,
                logitBias = if (model.contains("gpt-5") || model.contains("o1") || model.contains("o3")) null else logitBiasPreferences?.getLogitBiasesMap(),
                messages = msgs
            )
        } else {
            ChatCompletionRequest(
                model = ModelId(model),
                temperature = if (model.contains("gpt-5") || model.contains("o1") || model.contains("o3")) 1.0 else if (preferences!!.getTemperature().toDouble() == 0.7) null else preferences!!.getTemperature().toDouble(),
                topP = if (preferences!!.getTopP().toDouble() == 1.0) null else preferences!!.getTopP().toDouble(),
                frequencyPenalty = if (preferences!!.getFrequencyPenalty().toDouble() == 0.0) null else preferences!!.getFrequencyPenalty().toDouble(),
                presencePenalty = if (preferences!!.getPresencePenalty().toDouble() == 0.0) null else preferences!!.getPresencePenalty().toDouble(),
                seed = if (preferences!!.getSeed() != "") preferences!!.getSeed().toInt() else null,
                messages = msgs
            )
        }

        val completions: Flow<ChatCompletionChunk> =
            ai!!.chatCompletions(chatCompletionRequest)

        scroll(true)

        completions.collect { v ->
            run {
                if (!currentCoroutineContext().isActive) throw CancellationException()
                else if (v.choices[0].delta != null && v.choices[0].delta?.content != null && v.choices[0].delta?.content.toString() != "null") {
                    response += v.choices[0].delta?.content
                    messages[messages.size - 1]["message"] = response
                    if (messages.size > 2) {
                        adapter?.notifyItemRangeChanged(messages.size - 3, messages.size - 1)
                    } else {
                        adapter?.notifyItemChanged(messages.size - 1)
                    }
                    scroll(false)
                    saveSettings()
                }
            }
        }

        messages[messages.size - 1]["message"] = "$response\n"
        if (messages.size > 2) {
            adapter?.notifyItemRangeChanged(messages.size - 3, messages.size - 1)
        } else {
            adapter?.notifyItemChanged(messages.size - 1)
        }

        syncChatProjection()

        pronounce(shouldPronounce, response)

        saveSettings()
        calculateCost()

        btnMicro?.isEnabled = true
        btnSend?.isEnabled = true
        progress?.visibility = View.GONE
        messageInput?.requestFocus()

        // Put timestamp to chat to sort chats by last message
        ChatPreferences.getChatPreferences().putTimestampToChatById(this, chatId)

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
                    model = ModelId("gpt-4o"),
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
                    val logitBiasConfigId = preferences.getLogitBiasesConfigId()
                    val temperature = preferences.getTemperature()
                    val topP = preferences.getTopP()
                    val frequencyPenalty = preferences.getFrequencyPenalty()
                    val presencePenalty = preferences.getPresencePenalty()
                    val avatarType = preferences.getAvatarType()
                    val avatarId = preferences.getAvatarId()
                    val assistantName = preferences.getAssistantName()

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
                    preferences.setLogitBiasesConfigId(logitBiasConfigId)
                    preferences.setTemperature(temperature)
                    preferences.setTopP(topP)
                    preferences.setFrequencyPenalty(frequencyPenalty)
                    preferences.setPresencePenalty(presencePenalty)
                    preferences.setAvatarType(avatarType)
                    preferences.setAvatarId(avatarId)
                    preferences.setAssistantName(assistantName)

                    activityTitle?.text = newChatName.toString()

                    val i = Intent(this, ChatActivity::class.java).setAction(Intent.ACTION_VIEW).putExtra("chatId", Hash.hash(newChatName.toString())).putExtra("name", newChatName.toString())
                    startActivity(i)
                    finishActivity()
                } catch (_: Exception) { /* model might not be available */ }
            }
        }

        messageCounter++
    }

    private fun pronounce(st: Boolean, message: String) {
        if ((st && isTTSInitialized && !silenceMode) || preferences!!.getNotSilence()) {
            if (autoLangDetect) {
                try {
                    languageIdentifier = LanguageIdentification.getClient()
                    languageIdentifier?.identifyLanguage(message)
                        ?.addOnSuccessListener { languageCode ->
                            if (languageCode == "und") {
                                Log.i("MLKit", "Can't identify language.")
                            } else {
                                Log.i("MLKit", "Language: $languageCode")
                                tts!!.language = Locale.forLanguageTag(
                                    languageCode
                                )
                            }

                            speak(message)
                        }?.addOnFailureListener {
                            // Ignore auto language detection if an error is occurred
                            autoLangDetect = false
                            ttsPostInit()

                            speak(message)
                        }
                } catch (_: NullPointerException) {
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
            if (openAIKey == null) {
                openAIMissing("tts", message)
            } else {
                speakScope = CoroutineScope(Dispatchers.Main)

                speakScope?.launch {
                    progress?.setOnClickListener {
                        cancel()
                        restoreUIState()
                    }

                    try {
                        val rawAudio = openAIAI!!.speech(
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

                                val fis = FileInputStream(tempMp3)
                                mediaPlayer?.setDataSource(fis.fd)
                                mediaPlayer?.prepare()
                                mediaPlayer?.start()
                            } catch (ex: IOException) {
                                MaterialAlertDialogBuilder(this@ChatActivity, R.style.App_MaterialAlertDialog)
                                    .setTitle(R.string.label_audio_error)
                                    .setPositiveButton(R.string.btn_close) { _, _ -> }
                                    .setMessage(ex.stackTraceToString())
                                    .show()
                            }
                        }
                    } catch (_: CancellationException) {
                        restoreUIState()
                    }
                }
            }
        }
    }

    private fun writeImageToCache(bytes: ByteArray, imageType: String = "png") {
        try {
            contentResolver.openFileDescriptor(Uri.fromFile(File(getExternalFilesDir("images")?.absolutePath + "/" + Hash.hash(java.util.Base64.getEncoder().encodeToString(bytes)) + "." + imageType)), "w")?.use { fileDescriptor ->
                FileOutputStream(fileDescriptor.fileDescriptor).use {
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

    fun generateImageAsync(
        client: OpenAIClient,
        params: ImageGenerateParams,
        onSuccess: (String) -> Unit,
        onError: (Throwable) -> Unit
    ) : Job {
        return CoroutineScope(Dispatchers.IO).launch {
            progress?.setOnClickListener {
                cancel()
                restoreUIState()
            }

            try {
                var imageId: String
                val response = client.images().generate(params)
                val data: Optional<List<Image>> = response.data()
                val images = data.orElse(emptyList())

                val b64 = images.firstOrNull()?.b64Json()?.get()
                    ?: throw NullPointerException("Base64 string is null or empty, stopping...")

                val byteArray = Base64.decode(b64, Base64.DEFAULT)
                writeImageToCache(byteArray)
                imageId = Hash.hash(b64)

                withContext(Dispatchers.Main) {
                    onSuccess(imageId)
                }
            } catch (_: CancellationException) {
                withContext(Dispatchers.Main) {
                    onSuccess("cancelled")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private suspend fun generateImageR(p: String) {
        runOnUiThread {
            btnMicro?.isEnabled = false
            btnSend?.isEnabled = false
            progress?.visibility = View.VISIBLE
        }

        chat?.setOnTouchListener(null)
        disableAutoScroll = false
        // chat?.transcriptMode = ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL
        try {
            if (preferences!!.getImageModel().contains("gpt-image-")) {
                val client: OpenAIClient = OpenAIOkHttpClient
                    .builder()
                    .baseUrl(apiEndpointPreferences!!.getApiEndpoint(this, preferences!!.getApiEndpointId()).host)
                    .apiKey(apiEndpointPreferences!!.getApiEndpoint(this, preferences!!.getApiEndpointId()).apiKey)
                    .build()

                val params = ImageGenerateParams.builder()
                    .prompt(p)
                    .model(preferences!!.getImageModel())
                    .n(1L)
                    .quality(ImageGenerateParams.Quality.AUTO) // Settings param "quality" does not exists yet.
                    .size(ImageGenerateParams.Size._1024X1024) // Settings param "resolution" is ignored as this model supports only 1024x1024 resolution
                    .build()

                generateGptImageJob = generateImageAsync(
                    client,
                    params,
                    onSuccess = { file ->
                        if (file == "cancelled") {
                            runOnUiThread {
                                restoreUIState()
                            }
                            return@generateImageAsync
                        }

                        runOnUiThread {
                            putMessage("~file:$file", true)

                            chat?.setOnTouchListener { _, event ->
                                run {
                                    if (event.action == MotionEvent.ACTION_SCROLL || event.action == MotionEvent.ACTION_UP) {
                                        // chat?.transcriptMode = ListView.TRANSCRIPT_MODE_DISABLED
                                        disableAutoScroll = true
                                    }
                                    return@setOnTouchListener false
                                }
                            }

                            scroll(true)
                            scroll(false)

                            saveSettings()

                            btnMicro?.isEnabled = true
                            btnSend?.isEnabled = true
                            progress?.visibility = View.GONE

                            messageInput?.requestFocus()

                            // Put timestamp to chat to sort chats by last message
                            ChatPreferences.getChatPreferences().putTimestampToChatById(this@ChatActivity, chatId)
                            initSettings()
                        }
                    },
                    onError = { error ->
                        runOnUiThread {
                            if (preferences?.showChatErrors() == true) {
                                putMessage(
                                    when (error) {
                                        else -> error.stackTraceToString()
                                    }, true
                                )
                            }
                            btnMicro?.isEnabled = true
                            btnSend?.isEnabled = true
                            progress?.visibility = View.GONE
                            messageInput?.requestFocus()
                        }
                    }
                )
            } else {
                val images = openAIAI?.imageURL(
                    creation = ImageCreation(
                        prompt = p,
                        model = ModelId(preferences!!.getImageModel()),
                        n = 1,
                        size = ImageSize(resolution)
                    )
                )

                val imageUrl = images?.get(0)?.url!!

                val url = URL(imageUrl)

                val `is` = withContext(Dispatchers.IO) {
                    url.openStream()
                }
                var file = ""
                val th = Thread {
                    val bytes: ByteArray = org.apache.commons.io.IOUtils.toByteArray(`is`)

                    writeImageToCache(bytes)

                    val encoded = java.util.Base64.getEncoder().encodeToString(bytes)

                    file = Hash.hash(encoded)
                }

                th.start()
                withContext(Dispatchers.IO) {
                    th.join()
                    runOnUiThread {
                        putMessage("~file:$file", true)

                        chat?.setOnTouchListener { _, event ->
                            run {
                                if (event.action == MotionEvent.ACTION_SCROLL || event.action == MotionEvent.ACTION_UP) {
                                    // chat?.transcriptMode = ListView.TRANSCRIPT_MODE_DISABLED
                                    disableAutoScroll = true
                                }
                                return@setOnTouchListener false
                            }
                        }

                        scroll(true)
                        scroll(false)

                        saveSettings()

                        btnMicro?.isEnabled = true
                        btnSend?.isEnabled = true
                        progress?.visibility = View.GONE

                        messageInput?.requestFocus()

                        // Put timestamp to chat to sort chats by last message
                        ChatPreferences.getChatPreferences().putTimestampToChatById(this@ChatActivity, chatId)
                        initSettings()
                    }
                }
            }
        } catch (_: CancellationException) {
            runOnUiThread {
                restoreUIState()
            }
        } catch (e: Exception) {
            if (preferences?.showChatErrors() == true) {
                putMessage(
                    when {
                        e.stackTraceToString().contains("invalid model") -> {
                            getString(R.string.prompt_no_model_provided)
                        }
                        e.stackTraceToString().contains("Your request was rejected") -> {
                            getString(R.string.prompt_rejected)
                        }

                        e.stackTraceToString().contains("No address associated with hostname") -> {
                            getString(R.string.prompt_offline)
                        }

                        e.stackTraceToString().contains("Incorrect API key") -> {
                            getString(R.string.prompt_key_invalid)
                        }

                        e.stackTraceToString().contains("Software caused connection abort") -> {
                            getString(R.string.prompt_error_unknown)
                        }

                        e.stackTraceToString().contains("You exceeded your current quota") -> {
                            getString(R.string.prompt_quota_reached)
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
            if (!preferences!!.getImageModel().contains("gpt-image-")) {
                runOnUiThread {
                    restoreUIState()
                }
            }
        }
    }

    private fun findLastUserMessage(): HashMap<String, Any> {
        var lastUserMessage = hashMapOf<String, Any>()

        for (i in messages.size - 1 downTo 0) {
            if (messages[i]["isBot"] == false) {
                lastUserMessage = messages[i]
                break
            }
        }

        return lastUserMessage
    }

    private fun removeLastAssistantMessageIfAvailable() {
        if (messages.isNotEmpty() && messages.size - 1 > 0 && messages[messages.size - 1]["isBot"] == true) {
            // messages.removeAt(messages.size - 1)
            adapter?.onDelete(messages.size - 1)
        }

        if (chatMessages.isNotEmpty() && chatMessages.size - 1 > 0 && chatMessages[chatMessages.size - 1].role == Role.Assistant) {
            chatMessages.removeAt(chatMessages.size - 1)
        }
    }

    override fun onRetryClick() {
        removeLastAssistantMessageIfAvailable()
        saveSettings()

        val message = findLastUserMessage()

        if (message["image"] != null) {
            btnMicro?.isEnabled = false
            btnSend?.isEnabled = false
            progress?.visibility = View.VISIBLE

            val uri = Uri.fromFile(File(getExternalFilesDir("images")?.absolutePath + "/" + message["image"] + "." + message["imageType"]))
            imageIsSelected = true
            bitmap = readFile(uri)

            if (bitmap != null) {
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

        parseMessage(message["message"].toString(), false)
    }

    private fun syncChatProjection() {
        if (chatMessages == null) chatMessages = arrayListOf()

        if (chatMessages.isNotEmpty()) chatMessages.clear()

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

        updateMessagesSelectionProjection()
        calculateCost()
    }

    override fun onMessageEdited() {
        syncChatProjection()
    }

    override fun onMessageDeleted() {
        syncChatProjection()
    }

    @SuppressLint("SetTextI18n")
    override fun onBulkSelectionChanged(position: Int, selected: Boolean) {
        messagesSelectionProjection[position]["selected"] = selected
        selectedCount?.text = messagesSelectionProjection.count { it["selected"] == true }.toString()
    }

    @Suppress("deprecation")
    override fun onChangeBulkActionMode(mode: Boolean) {
        bulkSelectionMode = mode

        if (mode) {
            if (Build.VERSION.SDK_INT < 30) {
                window.statusBarColor = ResourcesCompat.getColor(resources, R.color.accent_250, theme)
            }
            bulkContainer?.visibility = View.VISIBLE
        } else {
            reloadAmoled()
            bulkContainer?.visibility = View.GONE
        }
    }

    private fun openAIMissing(feature: String, prompt: String) {
        restoreUIState()

        val message = when(feature) {
            "dalle" -> "Image generation"
            "tts" -> "OpenAI text-to-speech"
            "whisper" -> "Whisper speech recognition"
            else -> "this OpenAI"
        }

        MaterialAlertDialogBuilder(
            this,
            R.style.App_MaterialAlertDialog
        )
            .setTitle("OpenAI API endpoint missing")
            .setMessage("To use $message, you need to add OpenAI API endpoint first. Would you like to add OpenAI endpoint now?")
            .setPositiveButton(R.string.yes) { _, _ -> requestAddApiEndpoint(feature, prompt) }
            .setNegativeButton(R.string.no) { _, _ -> onCancelOpenAIAction(feature) }
            .show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (imageIsSelected) {
            outState.putString("image", baseImageString)
            outState.putString("imageType", selectedImageType)
        }
        super.onSaveInstanceState(outState)
    }

    private fun base64ToBitmap(base64Str: String): Bitmap? {
        return try {
            // Decode Base64 string to bytes
            val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
            // Decode byte array to Bitmap
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (_: IllegalArgumentException) {
            // Handle the case where the Base64 string was not correctly formatted
            null
        }
    }

    private fun onRestoredState(savedInstanceState: Bundle?) {
        val image = savedInstanceState?.getString("image")

        if (image != null) {
            baseImageString = image
            imageIsSelected = true
            selectedImageType = savedInstanceState.getString("imageType")

            bitmap = base64ToBitmap(baseImageString!!.split(",")[1])

            if (bitmap != null) {
                attachedImage?.visibility = View.VISIBLE

                val resizedBitmap = resizeBitmapToMaxHeight(bitmap!!, 100)
                selectedImage?.setImageBitmap(roundCorners(resizedBitmap))
            }
        }
    }

    private fun requestAddApiEndpoint(feature: String, prompt: String) {
        val apiEndpointDialog: EditApiEndpointDialogFragment = EditApiEndpointDialogFragment.newInstance("OpenAI", "https://api.openai.com/v1/", "", -1)
        apiEndpointDialog.setListener(object : EditApiEndpointDialogFragment.StateChangesListener {
            override fun onAdd(apiEndpoint: ApiEndpointObject) {
                apiEndpointPreferences?.setApiEndpoint(this@ChatActivity, apiEndpoint)
                openAIKey = apiEndpoint.apiKey

                val configOpenAI = OpenAIConfig(
                    token = openAIKey.toString(),
                    logging = LoggingConfig(LogLevel.None, Logger.Simple),
                    timeout = Timeout(socket = 30.seconds),
                    organization = null,
                    headers = emptyMap(),
                    host = OpenAIHost(apiEndpoint.host),
                    proxy = null,
                    retry = RetryStrategy()
                )
                openAIAI = OpenAI(configOpenAI)
                onOpenAIAction(feature, prompt)
            }

            override fun onError(message: String, position: Int) {
                apiEndpointDialog.show(supportFragmentManager, "EditApiEndpointDialogFragment")
            }

            override fun onCancel(position: Int) {
                onCancelOpenAIAction(feature)
            }
        })
        apiEndpointDialog.show(supportFragmentManager, "EditApiEndpointDialogFragment")
    }

    private fun onCancelOpenAIAction(feature: String) {
        if (feature == "dalle") {
            putMessage("DALL-E image generation is disabled. Please add OpenAI API endpoint to enable this feature.", true)
            saveSettings()
        }
    }

    private fun onOpenAIAction(feature: String, prompt: String) {
        when (feature) {
            "dalle" -> {
                btnMicro?.isEnabled = false
                btnSend?.isEnabled = false
                progress?.visibility = View.VISIBLE

                CoroutineScope(Dispatchers.Main).launch {
                    progress?.setOnClickListener {
                        cancel()
                        restoreUIState()
                    }

                    generateImageR(prompt)
                }
            }
            "tts" -> speak(prompt)
            "whisper" -> handleWhisperSpeechRecognition()
        }
    }

    private fun updateMessagesSelectionProjection() {
        bulkSelectionMode = false
        adapter?.setBulkActionMode(false)

        messagesSelectionProjection.clear()

        for (m in messages) {
            messagesSelectionProjection.add(
                java.util.HashMap(
                    mapOf(
                        "message" to m["message"],
                        "isBot" to m["isBot"],
                        "image" to m["image"],
                        "imageType" to m["imageType"],
                        "selected" to false
                    )
                )
            )
        }
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    private fun selectAll() {
        adapter?.selectAll()

        for (i in messagesSelectionProjection.indices) {
            messagesSelectionProjection[i]["selected"] = true
        }

        selectedCount?.text = messagesSelectionProjection.size.toString()
        bulkSelectionMode = true
        bulkContainer?.visibility = View.VISIBLE
        adapter?.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun deselectAll() {
        adapter?.unselectAll()

        for (i in messagesSelectionProjection.indices) {
            messagesSelectionProjection[i]["selected"] = false
        }

        selectedCount?.text = "0"
        bulkSelectionMode = false
        bulkContainer?.visibility = View.GONE
        adapter?.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun deleteSelectedMessages() {
        MaterialAlertDialogBuilder(this, R.style.App_MaterialAlertDialog)
            .setTitle("Delete selected messages")
            .setMessage("Are you sure you want to delete selected messages?")
            .setPositiveButton("Delete") { _, _ ->
                var pos = 0
                var p = 0
                while (pos < messagesSelectionProjection.size) {
                    if (messagesSelectionProjection[pos]["selected"].toString() == "true") {
                        messages.removeAt(pos - p)
                        p++
                    }

                    pos++
                }

                syncChatProjection()
                saveSettings()
                adapter?.notifyDataSetChanged()
                updateMessagesSelectionProjection()
                deselectAll()
                calculateCost()
            }
            .setNegativeButton("Cancel") { _, _ -> }
            .show()
    }

    private fun copySelectedMessages() {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied messages", conversationToString())
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Messages copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun shareSelectedMessages() {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, conversationToString())
        startActivity(Intent.createChooser(intent, "Share messages"))
    }

    private fun conversationToString() : String {
        val stringBuilder = StringBuilder()

        for (m in messagesSelectionProjection) {
            if (m["selected"].toString() == "true") {
                if (m["isBot"] == true) {
                    stringBuilder.append("[Bot] >\n")
                } else {
                    stringBuilder.append("[User] >\n")
                }
                stringBuilder.append(m["message"])
                stringBuilder.append("\n\n")
            }
        }

        return stringBuilder.toString()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        adjustPaddings()
    }

    private fun adjustPaddings() {
        WindowInsetsUtil.adjustPaddings(this, R.id.action_bar, EnumSet.of(WindowInsetsUtil.Companion.Flags.STATUS_BAR))
        WindowInsetsUtil.adjustPaddings(this, R.id.bulk_container, EnumSet.of(WindowInsetsUtil.Companion.Flags.STATUS_BAR))
        WindowInsetsUtil.adjustPaddings(this, R.id.keyboard_frame, EnumSet.of(WindowInsetsUtil.Companion.Flags.NAVIGATION_BAR))
        WindowInsetsUtil.adjustPaddings(this, R.id.messages, EnumSet.of(WindowInsetsUtil.Companion.Flags.NAVIGATION_BAR))

        val messages = findViewById<RecyclerView>(R.id.messages)
        val layoutParams = messages.layoutParams as ViewGroup.MarginLayoutParams

        if (Build.VERSION.SDK_INT >= 30) {
            layoutParams.topMargin = dpToPx(64) + window.decorView.rootWindowInsets.getInsets(WindowInsets.Type.statusBars()).top
        } else {
            val view = findViewById<View>(android.R.id.content) ?: return
            ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
                layoutParams.topMargin = dpToPx(64) + insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
                insets
            }
        }

        messages.layoutParams = layoutParams
    }

    private fun finishActivity() {
        val root: View = findViewById(R.id.root)
        root.animate().alpha(0f).setDuration(200)
        supportFinishAfterTransition()
    }
}
