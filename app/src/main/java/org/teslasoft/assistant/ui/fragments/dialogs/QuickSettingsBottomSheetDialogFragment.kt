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

package org.teslasoft.assistant.ui.fragments.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.ApiEndpointPreferences
import org.teslasoft.assistant.preferences.FavoriteModelsPreferences
import org.teslasoft.assistant.preferences.LogitBiasConfigPreferences
import org.teslasoft.assistant.preferences.Preferences
import org.teslasoft.assistant.preferences.dto.ApiEndpointObject
import org.teslasoft.assistant.ui.activities.ApiEndpointsListActivity
import org.teslasoft.assistant.ui.activities.LogitBiasConfigListActivity

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
    private var btnSelectLogitBias: ConstraintLayout? = null
    private var btnSelectApiEndpoint: ConstraintLayout? = null
    private var apiEndpointPreferences: ApiEndpointPreferences? = null
    private var apiEndpoint: ApiEndpointObject? = null

    private var logitBiasConfigPreferences: LogitBiasConfigPreferences? = null

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
    private var textHost: TextView? = null
    private var textLogitBiasesConfig: TextView? = null
    private var favoriteModelsPreferences: FavoriteModelsPreferences? = null

    private var logitBiasesActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val configId = data?.getStringExtra("configId")

            if (configId != null) {
                preferences?.setLogitBiasesConfigId(configId)
                textLogitBiasesConfig?.text = if (configId != ""){
                    logitBiasConfigPreferences?.getConfigById(configId)?.get("label") ?: "Tap to set"
                } else {
                    "Tap to set"
                }
                shouldForceUpdate = true
            }
        }
    }

    private var apiEndpointActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val apiEndpointId = data?.getStringExtra("apiEndpointId")

            if (apiEndpointId != null) {
                preferences?.setApiEndpointId(apiEndpointId)
                apiEndpoint = apiEndpointPreferences?.getApiEndpoint(requireContext(), apiEndpointId)
                textHost?.text = apiEndpoint?.host ?: "Tap to set"
                shouldForceUpdate = true
            }
        }
    }

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

    private var modelSelectedListenerV2: AdvancedFavoriteModelSelectorDialogFragment.OnModelSelectedListener = AdvancedFavoriteModelSelectorDialogFragment.OnModelSelectedListener { model ->
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
        apiEndpointPreferences = ApiEndpointPreferences.getApiEndpointPreferences(requireContext())
        apiEndpoint = apiEndpointPreferences?.getApiEndpoint(requireContext(), preferences?.getApiEndpointId()!!)
        logitBiasConfigPreferences = LogitBiasConfigPreferences.getLogitBiasConfigPreferences(requireContext())
        favoriteModelsPreferences = FavoriteModelsPreferences.getPreferences(requireContext())

        btnSelectModel = view.findViewById(R.id.btn_select_model)
        btnSelectSystemMessage = view.findViewById(R.id.btn_select_system)
        btnSelectLogitBias = view.findViewById(R.id.btn_set_logit_biases)
        btnSelectApiEndpoint = view.findViewById(R.id.btn_select_api_endpoint)
        temperatureSeekbar = view.findViewById(R.id.temperature_slider)
        frequencyPenaltySeekbar = view.findViewById(R.id.frequency_penalty_slider)
        presencePenaltySeekbar = view.findViewById(R.id.presence_penalty_slider)
        topPSeekbar = view.findViewById(R.id.top_p_slider)
        fieldSeed = view.findViewById(R.id.field_seed)
        textModel = view.findViewById(R.id.text_model)
        textHost = view.findViewById(R.id.text_host)
        textLogitBiasesConfig = view.findViewById(R.id.text_logit_biases_config)

        textHost?.text = apiEndpoint?.host ?: "Tap to set"
        textLogitBiasesConfig?.text = if (preferences?.getLogitBiasesConfigId() != ""){
            logitBiasConfigPreferences?.getConfigById(preferences?.getLogitBiasesConfigId()!!)?.get("label") ?: "Tap to set"
        } else {
            "Tap to set"
        }

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
            var favorites = favoriteModelsPreferences?.getFavoriteModels()

            if (favorites == null) favorites = arrayListOf()

            if (favorites.isEmpty()) {
                val dialog = AdvancedModelSelectorDialogFragment.newInstance(model!!, chatId)
                dialog.setModelSelectedListener(modelSelectedListener)
                dialog.show(parentFragmentManager, "AdvancedModelSelectorDialogFragment")
            } else {
                val dialog = AdvancedFavoriteModelSelectorDialogFragment.newInstance(model!!, chatId)
                dialog.setModelSelectedListener(modelSelectedListenerV2)
                dialog.show(parentFragmentManager, "AdvancedFavoriteModelSelectorDialogFragment")
            }
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

        btnSelectLogitBias?.setOnClickListener {
            logitBiasesActivityResultLauncher.launch(Intent(requireContext(), LogitBiasConfigListActivity::class.java))
        }

        btnSelectApiEndpoint?.setOnClickListener {
            apiEndpointActivityResultLauncher.launch(Intent(requireContext(), ApiEndpointsListActivity::class.java))
        }
    }

    interface OnUpdateListener {
        fun onUpdate()
        fun onForceUpdate()
    }
}