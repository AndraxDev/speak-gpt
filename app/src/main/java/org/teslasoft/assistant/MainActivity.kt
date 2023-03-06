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
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionChunk
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.ktor.util.reflect.Type
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.teslasoft.assistant.adapters.ChatAdapter
import org.teslasoft.assistant.onboarding.WelcomeActivity
import org.teslasoft.assistant.settings.SettingsActivity
import org.teslasoft.assistant.ui.MicrophonePermissionScreen
import java.util.Locale


class MainActivity : FragmentActivity() {

    // Init UI
    private var messageInput: EditText? = null
    private var btnSend: ImageButton? = null
    private var btnMicro: ImageButton? = null
    private var btnSettings: ImageButton? = null
    private var btnKeyboard: ImageButton? = null
    private var keyboardInput: LinearLayout? = null
    private var progress: ProgressBar? = null
    private var chat: ListView? = null
    private var activityTitle: TextView? = null

    // Init chat
    private var messages: ArrayList<HashMap<String, Any>> = ArrayList()
    private var adapter: ChatAdapter? = null

    // Init states
    private var isRecording = false
    private var keyboardMode = false
    private var isTTSInitialized = false
    private var silenceMode = false

    // init AI
    private var ai: OpenAI? = null
    private var key: String? = null

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

        setContentView(R.layout.activity_main)

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

    private fun initSettings() {
        val settings: SharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)

        key = settings.getString("api_key", null)

        if (key == null) {
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        } else {
            val silenceSettings: SharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)
            silenceMode = silenceSettings.getBoolean("silence_mode", false) == true

            val chat: SharedPreferences = getSharedPreferences("chat", MODE_PRIVATE)

            messages = try {
                val gson = Gson()
                val json = chat.getString("chat", null)
                val type: Type = object : TypeToken<ArrayList<HashMap<String, Any>?>?>() {}.type

                gson.fromJson<Any>(json, type) as ArrayList<HashMap<String, Any>>
            } catch (e: Exception) {
                ArrayList()
            }

            adapter = ChatAdapter(messages, this)

            initUI()
            initSpeechListener()
            initTTS()
            initLogic()
            initAI()
        }
    }

    private fun saveSettings() {
        val chat = getSharedPreferences("chat", MODE_PRIVATE)
        val editor = chat.edit()
        val gson = Gson()
        val json: String = gson.toJson(messages)

        editor.putString("chat", json)
        editor.apply()
    }

    @SuppressLint("SetTextI18n")
    private fun initUI() {
        btnMicro = findViewById(R.id.btn_micro)
        btnSettings = findViewById(R.id.btn_settings)
        btnKeyboard = findViewById(R.id.btn_keyboard)
        keyboardInput = findViewById(R.id.keyboard_input)
        chat = findViewById(R.id.messages)
        messageInput = findViewById(R.id.message_input)
        btnSend = findViewById(R.id.btn_send)
        progress = findViewById(R.id.progress)
        activityTitle = findViewById(R.id.activity_title)

        try {
            val pInfo: PackageInfo = this.packageManager.getPackageInfo(this.packageName, 0)
            val version = pInfo.versionName
            activityTitle?.text = "${resources.getString(R.string.app_name)} $version"
        } catch (e: PackageManager.NameNotFoundException) {
            activityTitle?.text = resources.getString(R.string.app_name)
        }

        progress?.visibility = View.GONE

        btnMicro?.setImageResource(R.drawable.ic_microphone)
        btnSettings?.setImageResource(R.drawable.ic_settings)
        btnKeyboard?.setImageResource(R.drawable.ic_keyboard)

        keyboardInput?.visibility = View.GONE

        chat?.adapter = adapter
        chat?.divider = ColorDrawable(0x3D000000)
        chat?.dividerHeight = 1

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

        btnKeyboard?.setOnClickListener {
            if (keyboardMode) {
                keyboardMode = false
                keyboardInput?.visibility = View.GONE
                messageInput?.isEnabled = false
                btnKeyboard?.setImageResource(R.drawable.ic_keyboard)
            } else {
                keyboardMode = true
                keyboardInput?.visibility = View.VISIBLE
                messageInput?.isEnabled = true
                btnKeyboard?.setImageResource(R.drawable.ic_keyboard_hide)
            }
        }

        btnSend?.setOnClickListener {
            tts!!.stop()
            if (messageInput?.text.toString() != "") {
                val message: String = messageInput?.text.toString()

                messageInput?.setText("")

                keyboardMode = false
                keyboardInput?.visibility = View.GONE
                messageInput?.isEnabled = false
                btnKeyboard?.setImageResource(R.drawable.ic_keyboard)

                putMessage(message, false)

                btnMicro?.isEnabled = false
                btnSend?.isEnabled = false
                progress?.visibility = View.VISIBLE

                CoroutineScope(Dispatchers.Main).launch {
                    generateResponse(message, false)
                }
            }
        }

        btnSettings?.setOnClickListener {
            settingsLauncher.launch(
                Intent(
                    this,
                    SettingsActivity::class.java
                )
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
        }
    }

    /** SYSTEM INITIALIZATION END **/

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
        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo-0301"),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.User,
                    content = request
                )
            )
        )

        try {
            val completions: Flow<ChatCompletionChunk> = ai!!.chatCompletions(chatCompletionRequest)

            putMessage("", true)
            var response = ""

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

            messages[messages.size - 1]["message"] = "$response\n"
            adapter?.notifyDataSetChanged()

            if (shouldPronounce && isTTSInitialized && !silenceMode) {
                tts!!.speak(response, TextToSpeech.QUEUE_FLUSH, null,"")
            }
        } catch (e: Exception) {
            putMessage(e.stackTraceToString(), true)
        }

        saveSettings()

        btnMicro?.isEnabled = true
        btnSend?.isEnabled = true
        progress?.visibility = View.GONE
    }
}