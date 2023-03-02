package org.teslasoft.assistant

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.teslasoft.assistant.ui.MicrophonePermissionScreen
import java.lang.Exception
import java.util.Locale

class MainActivity : FragmentActivity() {

    private var btnMicro: ImageButton? = null
    private var text: TextView? = null
    private var responseText: TextView? = null
    private var recognizer: SpeechRecognizer? = null
    private var progress: ProgressBar? = null

    private var isRecording = false

    private var ai: OpenAI? = null;

    private val speechListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            /* unused */
        }

        override fun onBeginningOfSpeech() {

        }

        override fun onRmsChanged(rmsdB: Float) {
            /* unused */
        }

        override fun onBufferReceived(buffer: ByteArray?) {
            /* unused */
        }

        override fun onEndOfSpeech() {
            /* unused */
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
                text?.text = recognizedText
                responseText?.text = "Generating AI response..."

                progress?.visibility = View.VISIBLE

                CoroutineScope(Dispatchers.Main).launch {
                    generateResponse(recognizedText)
                }
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            /* unused */
        }

        override fun onEvent(eventType: Int, params: Bundle?) {
            /* unused */
        }
    }

    private val permissionResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        run {
            if (result.resultCode == Activity.RESULT_OK) {
                startRecognition()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        initUI()
        initSpeechListener()
        initLogic()
        initAI()
    }

    private fun initUI() {
        btnMicro = findViewById(R.id.btn_micro)
        btnMicro?.setImageResource(R.drawable.ic_microphone)

        text = findViewById(R.id.debug_text)
        responseText = findViewById(R.id.response)
        progress = findViewById(R.id.progress)
        progress?.visibility = View.GONE
    }

    private fun initLogic() {
        btnMicro?.setOnClickListener {
            if (isRecording) {
                btnMicro?.setImageResource(R.drawable.ic_microphone)
                recognizer?.stopListening()
                isRecording = false
            } else {
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
    }

    private fun startRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)

        recognizer?.startListening(intent)
    }

    private fun initSpeechListener() {
        recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recognizer?.setRecognitionListener(speechListener)
    }

    private fun initAI() {
        /*****************************************************************************
         * W A R N I N G
         * TODO: Obfuscate before release to prevent leaks and surprise bills
         *****************************************************************************/
        ai = OpenAI("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")

    }

    @OptIn(BetaOpenAI::class)
    private suspend fun generateResponse(request: String) {
        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo"),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.User,
                    content = request
                )
            )
        )

        try {
            val completion: ChatCompletion = ai!!.chatCompletion(chatCompletionRequest)

            val response = completion.choices[0].message?.content

            responseText?.text = response
        } catch (e: Exception) {
            responseText?.text = e.stackTraceToString()
        }

        progress?.visibility = View.GONE

        // val completions: Flow<ChatCompletionChunk> = ai.chatCompletions(chatCompletionRequest)
    }
}