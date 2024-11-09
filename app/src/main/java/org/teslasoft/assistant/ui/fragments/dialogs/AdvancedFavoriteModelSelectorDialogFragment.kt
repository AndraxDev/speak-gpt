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

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.loadingindicator.LoadingIndicator
import com.google.android.material.textfield.TextInputEditText
import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.ApiEndpointPreferences
import org.teslasoft.assistant.preferences.FavoriteModelsPreferences
import org.teslasoft.assistant.preferences.Preferences
import org.teslasoft.assistant.preferences.dto.ApiEndpointObject
import org.teslasoft.assistant.preferences.dto.FavoriteModelObject
import org.teslasoft.assistant.ui.adapters.FavoriteModelListAdapter
import org.teslasoft.assistant.util.Hash

class AdvancedFavoriteModelSelectorDialogFragment : DialogFragment() {

    companion object {
        fun newInstance(name: String, chatId: String) : AdvancedFavoriteModelSelectorDialogFragment {
            val advancedModelSelectorDialogFragment = AdvancedFavoriteModelSelectorDialogFragment()

            val args = Bundle()
            args.putString("name", name)
            args.putString("chatId", chatId)

            advancedModelSelectorDialogFragment.arguments = args

            return advancedModelSelectorDialogFragment
        }
    }

    private var apiEndpointPreferences: ApiEndpointPreferences? = null
    private var apiEndpointObject: ApiEndpointObject? = null
    private var listener: OnModelSelectedListener? = null
    private var progressBar: LoadingIndicator? = null
    private var ttsSelectorTitle: TextView? = null
    private var fieldSearch: TextInputEditText? = null
    private var builder: AlertDialog.Builder? = null
    private var modelList: ListView? = null

    private var modelListAdapter: FavoriteModelListAdapter? = null

    private var availableModels: ArrayList<Map<String, String>> = arrayListOf()
    private var availableModelsProjection: ArrayList<Map<String, String>> = arrayListOf()

    private var preferences: Preferences? = null
    private var favoriteModelsPreferences: FavoriteModelsPreferences? = null

    private var modelSelectedListener: AdvancedModelSelectorDialogFragment.OnModelSelectedListener = AdvancedModelSelectorDialogFragment.OnModelSelectedListener { model ->
        listener?.onModelSelected(model)
        dismiss()
    }

    private var modelClickListener = object : FavoriteModelListAdapter.OnItemClickListener {
        override fun onItemClick(model: String, endpointId: String) {
            listener?.onModelSelected(model)
            preferences?.setApiEndpointId(endpointId)
            Toast.makeText(requireActivity(), getString(R.string.msg_api_compatibility_change), Toast.LENGTH_SHORT).show()
            dismiss()
        }

        override fun onActionClick(model: String, endpointId: String, position: Int) {
            val modelObject = FavoriteModelObject(model, endpointId)
            availableModels.remove(hashMapOf("modelId" to modelObject.modelId, "endpointId" to modelObject.endpointId))
            availableModelsProjection.remove(hashMapOf("modelId" to modelObject.modelId, "endpointId" to modelObject.endpointId))
            favoriteModelsPreferences?.setFavoriteModels(availableModels)
            modelListAdapter?.notifyDataSetChanged()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        builder = MaterialAlertDialogBuilder(this.requireContext(), R.style.App_MaterialAlertDialog)

        val view: View = this.layoutInflater.inflate(R.layout.fragment_select_voice, null)

        modelList = view.findViewById(R.id.voices_list)
        ttsSelectorTitle = view.findViewById(R.id.tts_selector_title)
        fieldSearch = view.findViewById(R.id.field_search_text)

        fieldSearch?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { /* unused */ }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateProjection(s.toString().trim())
            }

            override fun afterTextChanged(s: Editable?) { /* unused */ }

        })

        modelList?.divider = null

        progressBar = view.findViewById(R.id.progressBar)

        ttsSelectorTitle?.text = getString(R.string.label_favorite_ai_models)

        progressBar?.visibility = View.GONE

        val model = requireArguments().getString("name")
        val chatId = requireArguments().getString("chatId")

        preferences = Preferences.getPreferences(requireActivity(), requireArguments().getString("chatId").toString())
        apiEndpointPreferences = ApiEndpointPreferences.getApiEndpointPreferences(requireActivity())
        apiEndpointObject = apiEndpointPreferences?.getApiEndpoint(requireActivity(), preferences?.getApiEndpointId()!!)
        favoriteModelsPreferences = FavoriteModelsPreferences.getPreferences(requireActivity())

        reloadList()

        builder!!.setView(view)
            .setCancelable(false)
            .setNeutralButton(R.string.btn_all_models) {_, _ -> run{
                val dialog = AdvancedModelSelectorDialogFragment.newInstance(model!!, chatId!!)
                dialog.setModelSelectedListener(modelSelectedListener)
                dialog.show(parentFragmentManager, "AdvancedModelSelectorDialogFragment")
            }}
            .setNegativeButton(android.R.string.cancel, null)

        return builder!!.create()
    }

    private fun reloadList() {
        val list = favoriteModelsPreferences?.getFavoriteModels()?.toMutableList()

        if (list != null) {
            availableModels.addAll(list)
        }

        updateProjection("")

        modelListAdapter = FavoriteModelListAdapter(requireContext(), availableModelsProjection, requireArguments().getString("chatId").toString(), preferences?.getApiEndpointId()!!)
        modelListAdapter?.setOnItemClickListener(modelClickListener)
        modelList?.adapter = modelListAdapter
        modelListAdapter?.notifyDataSetChanged()
    }

    fun interface OnModelSelectedListener {
        fun onModelSelected(model: String)
    }

    fun setModelSelectedListener(listener: OnModelSelectedListener) {
        this.listener = listener
    }

    private fun updateProjection(query: String) {
        if (availableModelsProjection == null) availableModelsProjection = arrayListOf()
        availableModelsProjection.clear()
        if (availableModelsProjection == null) availableModelsProjection = arrayListOf()

        if (query == "") {
            availableModelsProjection.addAll(availableModels)
        } else {
            availableModelsProjection = availableModels.filter { item -> item.get("modelId").toString() == query || item.get("modelId").toString().contains(query) || query.contains(item.get("modelId").toString())} as ArrayList<Map<String, String>>
        }

        modelListAdapter = FavoriteModelListAdapter(requireContext(), availableModelsProjection, requireArguments().getString("chatId").toString(), Hash.hash(apiEndpointObject?.label!!))
        modelListAdapter?.setOnItemClickListener(modelClickListener)
        modelList?.adapter = modelListAdapter
        modelListAdapter?.notifyDataSetChanged()
    }
}
