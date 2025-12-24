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

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.api.logging.Logger
import com.aallam.openai.api.model.Model
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost
import com.aallam.openai.client.RetryStrategy
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.loadingindicator.LoadingIndicator
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.ApiEndpointPreferences
import org.teslasoft.assistant.preferences.FavoriteModelsPreferences
import org.teslasoft.assistant.preferences.Preferences
import org.teslasoft.assistant.preferences.dto.ApiEndpointObject
import org.teslasoft.assistant.preferences.dto.FavoriteModelObject
import org.teslasoft.assistant.ui.adapters.ModelListAdapter
import org.teslasoft.assistant.util.Hash
import org.teslasoft.core.api.network.RequestNetwork
import kotlin.time.Duration.Companion.seconds

class AdvancedModelSelectorDialogFragment : DialogFragment() {
    companion object {
        fun newInstance(name: String, chatId: String) : AdvancedModelSelectorDialogFragment {
            val advancedModelSelectorDialogFragment = AdvancedModelSelectorDialogFragment()

            val args = Bundle()
            args.putString("name", name)
            args.putString("chatId", chatId)

            advancedModelSelectorDialogFragment.arguments = args

            return advancedModelSelectorDialogFragment
        }
    }

    private var builder: AlertDialog.Builder? = null
    private var modelList: ListView? = null
    private var progressBar: LoadingIndicator? = null
    private var ttsSelectorTitle: TextView? = null
    private var fieldSearch: TextInputEditText? = null

    private var preferences: Preferences? = null
    private var apiEndpointPreferences: ApiEndpointPreferences? = null
    private var favoriteModelsPreferences: FavoriteModelsPreferences? = null

    private var apiEndpointObject: ApiEndpointObject? = null
    private var listener: OnModelSelectedListener? = null

    private var availableModels: ArrayList<String> = arrayListOf()
    private var availableModelsProjection: ArrayList<String> = arrayListOf()

    private var requestNetwork: RequestNetwork? = null
    private var modelListAdapter: ModelListAdapter? = null

    private var mContext: Context? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        mContext = context
    }

    override fun onDetach() {
        super.onDetach()

        mContext = null
    }

    private var modelSelectedListener: ModelListAdapter.OnItemClickListener = object : ModelListAdapter.OnItemClickListener {
        override fun onItemClick(model: String) {
            val preferences = Preferences.getPreferences(mContext ?: return, requireArguments().getString("chatId").toString())

            preferences.setModel(model)
            modelListAdapter?.notifyDataSetChanged()
            listener?.onModelSelected(model)
            dismiss()
        }

        override fun onActionClick(model: String, endpointId: String, position: Int) {
            val m = FavoriteModelObject(model, endpointId)
            Toast.makeText(mContext ?: return, getString(R.string.label_added_to_favorites), Toast.LENGTH_SHORT).show()
            favoriteModelsPreferences?.addFavoriteModel(m)
        }
    }

    private var requestListener: RequestNetwork.RequestListener = object : RequestNetwork.RequestListener {
        override fun onResponse(tag: String, message: String) {
            val gson = com.google.gson.Gson()

            try {
                val models: Map<String, Any> = gson.fromJson(message, Map::class.java) as Map<String, Any>

                var modelsList: List<Map<String, Any>> = models["data"] as ArrayList<Map<String, Any>>

                if (modelsList == null) modelsList = arrayListOf()

                for (model in modelsList) {
                    val m = model.toMap()
                    availableModels.add(m["id"].toString())
                }

                updateProjection("")

                modelListAdapter = ModelListAdapter(requireContext(), availableModelsProjection, requireArguments().getString("chatId").toString(), Hash.hash(apiEndpointObject?.label!!))
                modelListAdapter?.setOnItemClickListener(modelSelectedListener)
                modelList?.divider = null
                modelList?.adapter = modelListAdapter
                modelListAdapter?.notifyDataSetChanged()
                progressBar?.visibility = View.GONE
            } catch (e: Exception) {
                if (context !== null) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.label_error)
                        .setMessage(getString(R.string.msg_model_loading_error_with_details) + e.message.toString())
                        .setPositiveButton(R.string.btn_ok) { _, _ -> this@AdvancedModelSelectorDialogFragment.dismiss() }
                        .show()
                }
            }
        }

        override fun onErrorResponse(tag: String, message: String) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.label_error)
                .setMessage(R.string.msg_error_loading_models)
                .setPositiveButton(R.string.btn_ok) { _, _ -> this@AdvancedModelSelectorDialogFragment.dismiss() }
                .show()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        builder = MaterialAlertDialogBuilder(this.requireContext(), R.style.App_MaterialAlertDialog)

        val view: View = this.layoutInflater.inflate(R.layout.fragment_select_voice, null)

        modelList = view.findViewById(R.id.voices_list)
        ttsSelectorTitle = view.findViewById(R.id.tts_selector_title)
        progressBar = view.findViewById(R.id.progressBar)
        fieldSearch = view.findViewById(R.id.field_search_text)

        builder!!.setView(view)
            .setCancelable(false)
            .setNegativeButton(android.R.string.cancel, null)

        fieldSearch?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { /* unused */ }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateProjection(s.toString().trim())
            }

            override fun afterTextChanged(s: Editable?) { /* unused */ }
        })

        ttsSelectorTitle?.text = getString(R.string.label_select_ai_model)

        progressBar?.visibility = View.VISIBLE

        preferences = Preferences.getPreferences(mContext ?: return builder!!.create(), requireArguments().getString("chatId").toString())
        apiEndpointPreferences = ApiEndpointPreferences.getApiEndpointPreferences(mContext ?: return builder!!.create())
        apiEndpointObject = apiEndpointPreferences?.getApiEndpoint(mContext ?: return builder!!.create(), preferences?.getApiEndpointId()!!)
        favoriteModelsPreferences = FavoriteModelsPreferences.getPreferences(mContext ?: return builder!!.create())

        val config = OpenAIConfig(
            token = apiEndpointObject?.apiKey!!,
            logging = LoggingConfig(LogLevel.None, Logger.Simple),
            timeout = Timeout(socket = 30.seconds),
            organization = null,
            headers = emptyMap(),
            host = OpenAIHost(apiEndpointObject?.host!!),
            proxy = null,
            retry = RetryStrategy()
        )
        val ai = OpenAI(config)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val models: List<Model> = ai.models()
                for (model in models) {
                    if (!model.id.id.contains("tts") && !model.id.id.contains("dall") && !model.id.id.contains("whisper") && !model.id.id.contains("embedding") && !model.id.id.contains("vision")) {
                        availableModels.add(model.id.id)
                    } else if (model.id.id.contains("ft:") || model.id.id.contains(":ft")) {
                        availableModels.add(model.id.id)
                    }
                }

                updateProjection("")

                modelListAdapter = ModelListAdapter(requireContext(), availableModelsProjection, requireArguments().getString("chatId").toString(), Hash.hash(apiEndpointObject?.label!!))
                modelListAdapter?.setOnItemClickListener(modelSelectedListener)
                modelList?.divider = null
                modelList?.adapter = modelListAdapter
                modelListAdapter?.notifyDataSetChanged()
                progressBar?.visibility = View.GONE
            } catch (_: Exception) {
                requestNetwork = RequestNetwork((mContext as Activity?) ?: return@launch)
                requestNetwork?.setHeaders(hashMapOf("Authorization" to "Bearer " + apiEndpointObject?.apiKey))
                requestNetwork?.startRequestNetwork("GET", apiEndpointObject?.host + "models", "A", requestListener)
            }
        }

        return builder!!.create()
    }

    private fun updateProjection(query: String) {
        if (availableModelsProjection == null) availableModelsProjection = arrayListOf()
        availableModelsProjection.clear()
        if (availableModelsProjection == null) availableModelsProjection = arrayListOf()

        if (query == "") {
            availableModelsProjection.addAll(availableModels)
        } else {
            availableModelsProjection = availableModels.filter { item -> item == query || item.contains(query) || query.contains(item)} as ArrayList<String>
        }

        modelListAdapter = ModelListAdapter(requireContext(), availableModelsProjection, requireArguments().getString("chatId").toString(), Hash.hash(apiEndpointObject?.label!!))
        modelListAdapter?.setOnItemClickListener(modelSelectedListener)
        modelList?.adapter = modelListAdapter
        modelListAdapter?.notifyDataSetChanged()
    }

    fun interface OnModelSelectedListener {
        fun onModelSelected(model: String)
    }

    fun setModelSelectedListener(listener: OnModelSelectedListener) {
        this.listener = listener
    }
}
