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

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.updatePadding
import androidx.core.widget.addTextChangedListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.ApiEndpointPreferences
import org.teslasoft.assistant.preferences.FavoriteModelsPreferences
import org.teslasoft.assistant.preferences.LogitBiasConfigPreferences
import org.teslasoft.assistant.preferences.Preferences
import org.teslasoft.assistant.preferences.dto.ApiEndpointObject
import org.teslasoft.assistant.ui.activities.ApiEndpointsListActivity
import org.teslasoft.assistant.ui.activities.LogitBiasConfigListActivity
import org.teslasoft.core.api.network.RequestNetwork

class QuickSettingsBottomSheetDialogFragment : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(chatId: String, usageIn: Int, usageOut: Int, priceIn: Float, priceOut: Float): QuickSettingsBottomSheetDialogFragment {
            val quickSettingsBottomSheetDialogFragment = QuickSettingsBottomSheetDialogFragment()

            val args = Bundle()
            args.putString("chatId", chatId)
            args.putInt("usageIn", usageIn)
            args.putInt("usageOut", usageOut)
            args.putFloat("priceIn", priceIn)
            args.putFloat("priceOut", priceOut)
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

    private var textUsage: TextView? = null
    private var textCost: TextView? = null
    private var textModel: TextView? = null
    private var textHost: TextView? = null
    private var textLogitBiasesConfig: TextView? = null
    private var favoriteModelsPreferences: FavoriteModelsPreferences? = null
    private var usageCost: ConstraintLayout? = null

    private var preferences: Preferences? = null
    private var chatId: String = ""

    private var updateListener: OnUpdateListener? = null
    private var shouldForceUpdate: Boolean = false

    private var priceIn = 0.0f
    private var priceOut = 0.0f
    private var usageIn = 0
    private var usageOut = 0

    private var isAttached = false

    private var requestNetwork: RequestNetwork? = null

    private var requestListener: RequestNetwork.RequestListener = object : RequestNetwork.RequestListener {
        override fun onResponse(tag: String, message: String) {
            val gson = com.google.gson.Gson()

            try {
                val models: Map<String, Any> = gson.fromJson(message, Map::class.java) as Map<String, Any>

                var modelsList: List<Map<String, Any>> = models["data"] as ArrayList<Map<String, Any>>

                if (modelsList == null) modelsList = arrayListOf()

                for (model in modelsList) {
                    val m = model.toMap()
                    if (preferences?.getModel() == m["id"]) {
                        priceIn = (m["pricing"] as Map<String, Any>)["prompt"].toString().toFloat()
                        priceOut = (m["pricing"] as Map<String, Any>)["completion"].toString().toFloat()
                        val costIn = priceIn * usageIn
                        val costOut = priceOut * usageOut
                        val costTotal = costIn + costOut
                        textCost?.text = String.format(getString(R.string.cost_template), costIn, costOut, costTotal, priceIn * 1000000, priceOut * 1000000)
                        break
                    }
                }
            } catch (e: Exception) {
                performStaticCostParse(preferences?.getModel()!!)
            }
        }

        override fun onErrorResponse(tag: String, message: String) {
            textCost?.text = getString(R.string.msg_error_calculating_cost)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        isAttached = true
    }

    override fun onDetach() {
        super.onDetach()

        isAttached = false
    }

    private var logitBiasesActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val configId = data?.getStringExtra("configId")

            if (configId != null) {
                preferences?.setLogitBiasesConfigId(configId)
                textLogitBiasesConfig?.text = if (configId != ""){
                    logitBiasConfigPreferences?.getConfigById(configId)?.get("label") ?: getString(R.string.label_tap_to_set)
                } else {
                    getString(R.string.label_tap_to_set)
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
                textHost?.text = if (apiEndpoint?.label != "") apiEndpoint?.label ?: getString(R.string.label_tap_to_set) else getString(R.string.label_tap_to_set)
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

    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun performStaticCostParse(model: String): HashMap<String, Float> {
        var inPrice = 0.0
        var outPrice = 0.0

        when {
            model.contains("gpt-4o") -> {
                inPrice = 0.000005
                outPrice = 0.000015
            }
        }

        if (isAttached) {
            if (inPrice == 0.0 && outPrice == 0.0) {
                textCost?.text = getString(R.string.msg_cost_not_enough_data)
            } else {
                val costIn = inPrice * usageIn
                val costOut = outPrice * usageOut
                val costTotal = costIn + costOut
                textCost?.text = String.format(getString(R.string.cost_template), costIn, costOut, costTotal, inPrice * 1000000, outPrice * 1000000)
            }
        }
        return hashMapOf()
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_quick_settings, container, false)
    }

    @SuppressLint("SetTextI18n")
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
        usageCost = view.findViewById(R.id.usage_cost)

        textUsage = view.findViewById(R.id.text_usage)
        textCost = view.findViewById(R.id.text_cost)

        textHost?.text = if (apiEndpoint?.label != "") apiEndpoint?.label ?: getString(R.string.label_tap_to_set) else getString(R.string.label_tap_to_set)
        textLogitBiasesConfig?.text = if (preferences?.getLogitBiasesConfigId() != ""){
            logitBiasConfigPreferences?.getConfigById(preferences?.getLogitBiasesConfigId()!!)?.get("label") ?: getString(R.string.label_tap_to_set)
        } else {
            getString(R.string.label_tap_to_set)
        }


        usageIn = requireArguments().getInt("usageIn")
        usageOut = requireArguments().getInt("usageOut")

        textUsage?.text = getString(R.string.cost_counter_usage).format(usageIn.toString(), usageOut.toString())
        textCost?.text = getString(R.string.cost_loading)

        if (usageIn >= 0) {
            requestNetwork = RequestNetwork(requireActivity())
            requestNetwork?.setHeaders(hashMapOf("Authorization" to "Bearer " + apiEndpoint?.apiKey))
            requestNetwork?.startRequestNetwork("GET", apiEndpoint?.host + "models", "A", requestListener)
        } else {
            textUsage?.text = "Usage: <Usage is not available in playground>"
            textCost?.text = "Cost: <Cost is not available in playground>"
            usageCost?.visibility = View.GONE
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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), R.style.ThemeOverlay_App_BottomSheetDialog)
    }

    interface OnUpdateListener {
        fun onUpdate()
        fun onForceUpdate()
    }
}
