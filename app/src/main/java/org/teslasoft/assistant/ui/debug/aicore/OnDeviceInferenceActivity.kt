package org.teslasoft.assistant.ui.debug.aicore

import android.os.Bundle
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.mlkit.genai.prompt.GenerativeModel
import com.google.mlkit.genai.prompt.ModelPreference
import org.teslasoft.assistant.R

class OnDeviceInferenceActivity : FragmentActivity() {

    private var textWarning: TextView? = null
    private var fieldMessage: TextInputEditText? = null
    private var textOutput: TextView? = null
    private var btnSend: MaterialButton? = null
    private var btnStop: MaterialButton? = null
    private var outputScroll: ScrollView? = null
    private var btnFast: RadioButton? = null
    private var btnFull: RadioButton? = null
    private var btnInit: MaterialButton? = null

    private var btnClear: ImageButton? = null

    private var loadingIndicator: LinearProgressIndicator? = null

    private var isFullModel: Boolean = false

    private val viewModel: NanoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_device_inference)

        textWarning = findViewById(R.id.text_warning)
        fieldMessage = findViewById(R.id.field_message)
        textOutput = findViewById(R.id.text_output)
        btnSend = findViewById(R.id.btn_send)
        btnStop = findViewById(R.id.btn_stop)
        outputScroll = findViewById(R.id.output_scroll)
        btnFast = findViewById(R.id.btn_fast)
        btnFull = findViewById(R.id.btn_full)
        btnInit = findViewById(R.id.btn_init)
        btnClear = findViewById(R.id.btn_clear)
        loadingIndicator = findViewById(R.id.progress_indicator)

        resetUI()
        initLogic()
    }

    private fun resetUI() {
        btnFast?.isChecked = true
        textOutput?.setTextIsSelectable(true)
        btnFast?.isEnabled = true
        btnFull?.isEnabled = true
        btnInit?.isEnabled = true
        loadingIndicator?.hide()
    }

    private fun initLogic() {
        btnSend?.setOnClickListener {
            val text = fieldMessage?.text.toString().trim()

            if (text.isNotEmpty()) {
                viewModel.generate(text)
            }
        }

        btnStop?.setOnClickListener {
            viewModel.cancelGeneration()
        }

        btnInit?.setOnClickListener {
            btnFast?.isEnabled = false
            btnFull?.isEnabled = false
            btnInit?.isEnabled = false

            val modelType: Int = if (isFullModel) {
                ModelPreference.FULL
            } else {
                ModelPreference.FAST
            }

            init(modelType)
        }

        btnFast?.setOnClickListener {
            isFullModel = false
        }

        btnFull?.setOnClickListener {
            isFullModel = true
        }

        btnClear?.setOnClickListener {
            textOutput?.text = ""
        }
    }

    private fun init(modelType: Int) {
        viewModel.initialize(modelType)
        viewModel.response.observe(this) { text ->
            textOutput?.text = text

            outputScroll?.post {
                outputScroll?.fullScroll(ScrollView.FOCUS_DOWN)
            }
        }

        viewModel.generating.observe(this) { running ->
            btnSend?.isEnabled = !running
            btnStop?.isEnabled = running

            if (running) {
                loadingIndicator?.show()
            } else {
                loadingIndicator?.hide()
            }
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
