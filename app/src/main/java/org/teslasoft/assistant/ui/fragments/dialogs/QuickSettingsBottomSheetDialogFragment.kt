package org.teslasoft.assistant.ui.fragments.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.Preferences

class QuickSettingsBottomSheetDialogFragment : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(chatId: String): QuickSettingsBottomSheetDialogFragment {
            val quickSettingsBottomSheetDialogFragment = QuickSettingsBottomSheetDialogFragment()

            val args = Bundle()
            args.putString("chatId", chatId)
            quickSettingsBottomSheetDialogFragment.arguments = args

            return quickSettingsBottomSheetDialogFragment
        }
    }

    private var btnSelectModel: ConstraintLayout? = null
    private var btnSelectSystemMessage: ConstraintLayout? = null

    private var temperatureSeekbar: com.google.android.material.slider.Slider? = null
    private var topPSeekbar: com.google.android.material.slider.Slider? = null
    private var frequencyPenaltySeekbar: com.google.android.material.slider.Slider? = null
    private var presencePenaltySeekbar: com.google.android.material.slider.Slider? = null
    private var fieldSeed: TextInputEditText? = null

    private var preferences: Preferences? = null
    private var chatId: String = ""

    private var updateListener: OnUpdateListener? = null

    private var shouldForceUpdate: Boolean = false

    private var textModel: TextView? = null

    private var systemChangedListener: SystemMessageDialogFragment.StateChangesListener =
        SystemMessageDialogFragment.StateChangesListener { prompt ->
            preferences?.setSystemMessage(prompt)
            shouldForceUpdate = true
            updateListener?.onUpdate()
        }

    private var modelSelectedListener: AdvancedModelSelectorDialogFragment.OnModelSelectedListener = AdvancedModelSelectorDialogFragment.OnModelSelectedListener { model ->
        preferences?.setModel(model)
        updateListener?.onUpdate()
        shouldForceUpdate = true
        textModel?.text = model
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        if (shouldForceUpdate) {
            updateListener?.onForceUpdate()
        }
    }

    fun setOnUpdateListener(listener: OnUpdateListener) {
        updateListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_quick_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatId = requireArguments().getString("chatId")!!
        preferences = Preferences.getPreferences(requireContext(), chatId)

        btnSelectModel = view.findViewById(R.id.btn_select_model)
        btnSelectSystemMessage = view.findViewById(R.id.btn_select_system)
        temperatureSeekbar = view.findViewById(R.id.temperature_slider)
        frequencyPenaltySeekbar = view.findViewById(R.id.frequency_penalty_slider)
        presencePenaltySeekbar = view.findViewById(R.id.presence_penalty_slider)
        topPSeekbar = view.findViewById(R.id.top_p_slider)
        fieldSeed = view.findViewById(R.id.field_seed)
        textModel = view.findViewById(R.id.text_model)

        temperatureSeekbar?.value = preferences?.getTemperature()!! * 10
        topPSeekbar?.value = preferences?.getTopP()!! * 10
        frequencyPenaltySeekbar?.value = preferences?.getFrequencyPenalty()!! * 10
        presencePenaltySeekbar?.value = preferences?.getPresencePenalty()!! * 10
        fieldSeed?.setText(preferences?.getSeed())

        val model = preferences?.getModel()

        if (model != null) {
            textModel?.text = model
        }

        btnSelectModel?.setOnClickListener {
            val dialog = AdvancedModelSelectorDialogFragment.newInstance(model!!, chatId)
            dialog.setModelSelectedListener(modelSelectedListener)
            dialog.show(parentFragmentManager, "AdvancedModelSelectorDialogFragment")
        }

        btnSelectSystemMessage?.setOnClickListener {
            val dialog = SystemMessageDialogFragment.newInstance(preferences?.getSystemMessage()!!)
            dialog.setStateChangedListener(systemChangedListener)
            dialog.show(parentFragmentManager, "SystemMessageDialogFragment")
        }

        fieldSeed?.addTextChangedListener { text ->
            preferences?.setSeed(text.toString())
        }

        temperatureSeekbar?.addOnChangeListener { _, value, _ ->
            preferences?.setTemperature(value / 10.0f)
        }

        temperatureSeekbar?.setLabelFormatter {
            return@setLabelFormatter "${it/10.0}"
        }

        topPSeekbar?.addOnChangeListener { _, value, _ ->
            preferences?.setTopP(value / 10.0f)
        }

        topPSeekbar?.setLabelFormatter {
            return@setLabelFormatter "${it/10.0}"
        }

        frequencyPenaltySeekbar?.addOnChangeListener { _, value, _ ->
            preferences?.setFrequencyPenalty(value / 10.0f)
        }

        frequencyPenaltySeekbar?.setLabelFormatter {
            return@setLabelFormatter "${it/10.0}"
        }

        presencePenaltySeekbar?.addOnChangeListener { _, value, _ ->
            preferences?.setPresencePenalty(value / 10.0f)
        }

        presencePenaltySeekbar?.setLabelFormatter {
            return@setLabelFormatter "${it/10.0}"
        }
    }

    interface OnUpdateListener {
        fun onUpdate()
        fun onForceUpdate()
    }
}