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

package org.teslasoft.assistant.ui.fragments.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.textfield.TextInputLayout
import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.Preferences

class AdvancedSettingsDialogFragment : BottomSheetDialogFragment() {
    companion object {
        fun newInstance(name: String, chatId: String) : AdvancedSettingsDialogFragment {
            val advancedSettingsDialogFragment = AdvancedSettingsDialogFragment()

            val args = Bundle()
            args.putString("name", name)
            args.putString("chatId", chatId)

            advancedSettingsDialogFragment.arguments = args

            return advancedSettingsDialogFragment
        }
    }

    private var gpt_35_turbo: RadioButton? = null
    private var gpt_4: RadioButton? = null
    private var gpt_4_turbo: RadioButton? = null
    private var gpt_4_o: RadioButton? = null
    private var gpt_5: RadioButton? = null
    private var gpt_5_mini: RadioButton? = null
    private var gpt_5_nano: RadioButton? = null
    private var o3_mini: RadioButton? = null
    private var o3: RadioButton? = null
    private var o1_mini: RadioButton? = null
    private var o1: RadioButton? = null
    private var see_all_models: RadioButton? = null
    private var see_favorite_models: RadioButton? = null
    private var ft: RadioButton? = null
    private var ftInput: EditText? = null
    private var maxTokens: EditText? = null
    private var endSeparator: EditText? = null
    private var prefix: EditText? = null
    private var ftFrame: TextInputLayout? = null
    private var temperatureSeekbar: com.google.android.material.slider.Slider? = null
    private var topPSeekbar: com.google.android.material.slider.Slider? = null
    private var frequencyPenaltySeekbar: com.google.android.material.slider.Slider? = null
    private var presencePenaltySeekbar: com.google.android.material.slider.Slider? = null
    private var btnSave: MaterialButton? = null
    private var btnCancel: MaterialButton? = null

    private var listener: StateChangesListener? = null

    private var model = "gpt-3.5-turbo"

    private var context: Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this.activity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_advanced_settings, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gpt_35_turbo = view.findViewById(R.id.gpt_35_turbo)
        gpt_4 = view.findViewById(R.id.gpt_4)
        gpt_4_turbo = view.findViewById(R.id.gpt_4_turbo)
        gpt_4_o = view.findViewById(R.id.gpt_4_o)
        gpt_5 = view.findViewById(R.id.gpt_5)
        gpt_5_mini = view.findViewById(R.id.gpt_5_mini)
        gpt_5_nano = view.findViewById(R.id.gpt_5_nano)
        o3_mini = view.findViewById(R.id.gpt_o3_mini)
        o3 = view.findViewById(R.id.gpt_o3)
        o1_mini = view.findViewById(R.id.gpt_o1_mini)
        o1 = view.findViewById(R.id.gpt_o1)
        see_all_models = view.findViewById(R.id.see_all_models)
        see_favorite_models = view.findViewById(R.id.see_favorite_models)
        ft = view.findViewById(R.id.ft)
        ftInput = view.findViewById(R.id.ft_input)
        maxTokens = view.findViewById(R.id.max_tokens)
        endSeparator = view.findViewById(R.id.end_separator)
        prefix = view.findViewById(R.id.prefix)
        ftFrame = view.findViewById(R.id.ft_frame)
        temperatureSeekbar = view.findViewById(R.id.temperature_slider)
        frequencyPenaltySeekbar = view.findViewById(R.id.frequency_penalty_slider)
        presencePenaltySeekbar = view.findViewById(R.id.presence_penalty_slider)
        topPSeekbar = view.findViewById(R.id.top_p_slider)
        btnSave = view.findViewById(R.id.btn_post)
        btnCancel = view.findViewById(R.id.btn_discard)

        val preferences: Preferences = Preferences.getPreferences(requireActivity(), arguments?.getString("chatId")!!)

        temperatureSeekbar?.value = preferences.getTemperature() * 10
        topPSeekbar?.value = preferences.getTopP() * 10
        frequencyPenaltySeekbar?.value = preferences.getFrequencyPenalty() * 10
        presencePenaltySeekbar?.value = preferences.getPresencePenalty() * 10

        temperatureSeekbar?.addOnChangeListener { _, value, _ ->
            preferences.setTemperature(value / 10.0f)
        }

        temperatureSeekbar?.setLabelFormatter {
            return@setLabelFormatter "${it/10.0}"
        }

        topPSeekbar?.addOnChangeListener { _, value, _ ->
            preferences.setTopP(value / 10.0f)
        }

        topPSeekbar?.setLabelFormatter {
            return@setLabelFormatter "${it/10.0}"
        }

        frequencyPenaltySeekbar?.addOnChangeListener { _, value, _ ->
            preferences.setFrequencyPenalty(value / 10.0f)
        }

        frequencyPenaltySeekbar?.setLabelFormatter {
            return@setLabelFormatter "${it/10.0}"
        }

        presencePenaltySeekbar?.addOnChangeListener { _, value, _ ->
            preferences.setPresencePenalty(value / 10.0f)
        }

        presencePenaltySeekbar?.setLabelFormatter {
            return@setLabelFormatter "${it/10.0}"
        }

        maxTokens?.setText(preferences.getMaxTokens().toString())
        endSeparator?.setText(preferences.getEndSeparator())
        prefix?.setText(preferences.getPrefix())

        bindClickListener(gpt_35_turbo, "gpt-3.5-turbo")
        bindClickListener(gpt_5, "gpt-5")
        bindClickListener(gpt_5_mini, "gpt-5-mini")
        bindClickListener(gpt_5_nano, "gpt-5-nano")
        bindClickListener(o3_mini, "o3-mini")
        bindClickListener(o3, "o3")
        bindClickListener(o1_mini, "o1-mini")
        bindClickListener(o1, "o1")
        bindClickListener(gpt_4, "gpt-4")
        bindClickListener(gpt_4_turbo, "gpt-4-turbo-preview")
        bindClickListener(gpt_4_o, "gpt-4o")

        ft?.setOnClickListener {
            setSelection(ft, ftInput?.text.toString(), hideFt = false, validateForm = false)
        }

        see_all_models?.setOnClickListener {
            val advancedModelSelectorDialogFragment = AdvancedModelSelectorDialogFragment.newInstance(model, requireArguments().getString("chatId").toString())
            advancedModelSelectorDialogFragment.setModelSelectedListener { model ->
                this@AdvancedSettingsDialogFragment.model = model

                reloadModelList(model)
                validateForm()
            }
            advancedModelSelectorDialogFragment.show(requireActivity().supportFragmentManager, "advancedModelSelectorDialogFragment")
        }

        see_favorite_models?.setOnClickListener {
            val advancedModelSelectorDialogFragment = AdvancedFavoriteModelSelectorDialogFragment.newInstance(model, requireArguments().getString("chatId").toString())
            advancedModelSelectorDialogFragment.setModelSelectedListener { model ->
                this@AdvancedSettingsDialogFragment.model = model

                reloadModelList(model)
                validateForm()
            }
            advancedModelSelectorDialogFragment.show(requireActivity().supportFragmentManager, "advancedFavoriteModelSelectorDialogFragment")
        }

        ftInput?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { /* unused */ }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { model = s.toString() }
            override fun afterTextChanged(s: Editable?) { /* unused */ }
        })

        btnSave?.setOnClickListener {
            validateForm()
            Toast.makeText(requireActivity(), "Settings saved", Toast.LENGTH_SHORT).show()
        }

        btnCancel?.setOnClickListener {
            dismiss()
        }

        model = requireArguments().getString("name").toString()
        reloadModelList(model)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), R.style.ThemeOverlay_App_BottomSheetDialog)
    }

    private fun reloadModelList(model: String) {
        when (model) { // load default model if settings not found
            "gpt-3.5-turbo" -> setSelection(gpt_35_turbo, null, hideFt = true, validateForm = false)
            "gpt-5" -> setSelection(gpt_5, null, hideFt = true, validateForm = false)
            "gpt-5-mini" -> setSelection(gpt_5_mini, null, hideFt = true, validateForm = false)
            "gpt-5-nano" -> setSelection(gpt_5_nano, null, hideFt = true, validateForm = false)
            "o3-mini" -> setSelection(o3_mini, null, hideFt = true, validateForm = false)
            "o3" -> setSelection(o3, null, hideFt = true, validateForm = false)
            "o1-mini" -> setSelection(o1_mini, null, hideFt = true, validateForm = false)
            "o1" -> setSelection(o1, null, hideFt = true, validateForm = false)
            "gpt-4" -> setSelection(gpt_4, null, hideFt = true, validateForm = false)
            "gpt-4-turbo-preview" -> setSelection(gpt_4_turbo, null, hideFt = true, validateForm = false)
            "gpt-4o" -> setSelection(gpt_4_o, null, hideFt = true, validateForm = false)
            else -> {
                setSelection(ft, model, hideFt = false, validateForm = false)
                ftInput?.setText(model)
            }
        }
    }

    private fun bindClickListener(view: RadioButton?, value: String?) {
        view?.setOnClickListener {
            setSelection(view, value)
        }
    }

    private fun setSelection(view: RadioButton?, value: String?, hideFt: Boolean = true, validateForm: Boolean = true) {
        clearSelection()
        clearSelection()
        view?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
        view?.background = getDarkAccentDrawableV2(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent)!!)
        if (value != null) model = value
        if (hideFt) ftFrame?.visibility = View.GONE else ftFrame?.visibility = View.VISIBLE
        if (validateForm) validateForm()
    }

    private fun clearSingleSelection(view: RadioButton?, isTop: Boolean = false, isBottom: Boolean = false) {
        var background = R.drawable.btn_accent_center
        if (isTop) background = R.drawable.btn_accent_top
        if (isBottom) background = R.drawable.btn_accent_bottom
        view?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), background)!!, requireActivity())
        view?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))
    }

    private fun clearSelection() {
        clearSingleSelection(gpt_35_turbo, isBottom = true)
        clearSingleSelection(gpt_5, isTop = true)
        clearSingleSelection(gpt_5_mini)
        clearSingleSelection(gpt_5_nano)
        clearSingleSelection(o3_mini)
        clearSingleSelection(o3)
        clearSingleSelection(o1_mini)
        clearSingleSelection(o1)
        clearSingleSelection(gpt_4)
        clearSingleSelection(gpt_4_turbo)
        clearSingleSelection(gpt_4_o)
        clearSingleSelection(ft, isBottom = true)
        clearSingleSelection(see_all_models)
        clearSingleSelection(see_favorite_models, isTop = true)
    }

    private fun getDarkAccentDrawable(drawable: Drawable, context: Context) : Drawable {
        DrawableCompat.setTint(DrawableCompat.wrap(drawable), getSurfaceColor(context))
        return drawable
    }

    private fun getDarkAccentDrawableV2(drawable: Drawable) : Drawable {
        DrawableCompat.setTint(DrawableCompat.wrap(drawable), getSurfaceColorV2())
        return drawable
    }

    private fun getSurfaceColor(context: Context) : Int {
        return SurfaceColors.SURFACE_3.getColor(context)
    }

    private fun getSurfaceColorV2() : Int {
        return requireActivity().getColor(R.color.accent_900)
    }

    private fun validateForm() {
        if (ftInput?.text.toString() == "" && ft?.isChecked == true) {
            listener!!.onFormError(model, maxTokens?.text.toString(), endSeparator?.text.toString(), prefix?.text.toString())
            return
        }

        listener!!.onSelected(model, maxTokens?.text.toString(), endSeparator?.text.toString(), prefix?.text.toString())
    }

    fun setStateChangedListener(listener: StateChangesListener) {
        this.listener = listener
    }

    interface StateChangesListener {
        fun onSelected(name: String, maxTokens: String, endSeparator: String, prefix: String)
        fun onFormError(name: String, maxTokens: String, endSeparator: String, prefix: String)
    }
}
