package org.teslasoft.assistant.ui.debug.aicore

import android.os.Bundle
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.mlkit.genai.prompt.GenerativeModel
import org.teslasoft.assistant.R

class OnDeviceInferenceActivity : FragmentActivity() {

    private var textWarning: TextView? = null
    private var fieldModelId: TextInputEditText? = null
    private var fieldMessage: TextInputEditText? = null
    private var textOutput: TextView? = null
    private var btnWarmup: MaterialButton? = null
    private var btnSend: MaterialButton? = null
    private var btnStop: MaterialButton? = null
    private var outputScroll: ScrollView? = null

    private val viewModel: NanoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_device_inference)

        textWarning = findViewById(R.id.text_warning)
        fieldModelId = findViewById(R.id.field_model_id)
        fieldMessage = findViewById(R.id.field_message)
        textOutput = findViewById(R.id.text_output)
        btnWarmup = findViewById(R.id.btn_warmup)
        btnSend = findViewById(R.id.btn_send)
        btnStop = findViewById(R.id.btn_stop)
        outputScroll = findViewById(R.id.output_scroll)

        textOutput?.setTextIsSelectable(true)

        viewModel.initialize()

        btnWarmup?.setOnClickListener {

        }

        btnSend?.setOnClickListener {
            val text = fieldMessage?.text.toString().trim()

            if (text.isNotEmpty()) {
                viewModel.generate(text)
            }
        }

        btnStop?.setOnClickListener {
            viewModel.cancelGeneration()
        }

        viewModel.response.observe(this) { text ->
            textOutput?.text = text

            outputScroll?.post {
                outputScroll?.fullScroll(ScrollView.FOCUS_DOWN)
            }
        }

        viewModel.generating.observe(this) { running ->
            btnSend?.isEnabled = !running
            btnStop?.isEnabled = running
        }

        viewModel.error.observe(this) { error ->
            if (error != null) {
                Toast.makeText(
                    this,
                    error,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}