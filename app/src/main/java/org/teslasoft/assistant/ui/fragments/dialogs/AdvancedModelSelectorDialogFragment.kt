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
import android.view.View
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.Preferences
import org.teslasoft.assistant.ui.adapters.ModelListAdapter
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

    private var listener: OnModelSelectedListener? = null

    private var modelList: ListView? = null

    private var modelListAdapter: ModelListAdapter? = null

    private var availableModels: ArrayList<String> = arrayListOf()

    private var progressBar: ProgressBar? = null

    private var ttsSelectorTitle: TextView? = null

    private var modelSelectedListener: ModelListAdapter.OnItemClickListener =
        ModelListAdapter.OnItemClickListener { model ->
            run {
                val preferences = Preferences.getPreferences(requireActivity(), requireArguments().getString("chatId").toString())

                preferences.setModel(model)
                modelListAdapter?.notifyDataSetChanged()
                listener?.onModelSelected(model)
                dismiss()
            }
        }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        builder = MaterialAlertDialogBuilder(this.requireContext())

        val view: View = this.layoutInflater.inflate(R.layout.fragment_select_voice, null)

        modelList = view.findViewById(R.id.voices_list)
        ttsSelectorTitle = view.findViewById(R.id.tts_selector_title)

        progressBar = view.findViewById(R.id.progressBar)

        ttsSelectorTitle?.text = "Select AI model"

        progressBar?.visibility = View.VISIBLE

        val preferences = Preferences.getPreferences(requireActivity(), requireArguments().getString("chatId").toString())

        val config = OpenAIConfig(
            token = preferences.getApiKey(requireActivity()),
            logging = LoggingConfig(LogLevel.None, Logger.Simple),
            timeout = Timeout(socket = 30.seconds),
            organization = null,
            headers = emptyMap(),
            host = OpenAIHost(preferences.getCustomHost()),
            proxy = null,
            retry = RetryStrategy()
        )
        val ai = OpenAI(config)

        CoroutineScope(Dispatchers.Main).launch {
            val models: List<Model> = ai.models()
            for (model in models) {
                if (!model.id.id.contains("tts") && !model.id.id.contains("dall") && !model.id.id.contains("whisper") && !model.id.id.contains("embedding")  && !model.id.id.contains("vision")) {
                    availableModels.add(model.id.id)
                } else if (model.id.id.contains("ft:") || model.id.id.contains(":ft")) {
                    availableModels.add(model.id.id)
                }
            }

            modelListAdapter = ModelListAdapter(requireContext(), availableModels, requireArguments().getString("chatId").toString())
            modelListAdapter?.setOnItemClickListener(modelSelectedListener)
            modelList?.divider = null
            modelList?.adapter = modelListAdapter
            modelListAdapter?.notifyDataSetChanged()
            progressBar?.visibility = View.GONE
        }

        builder!!.setView(view)
            .setCancelable(false)
            .setNegativeButton(android.R.string.cancel, null)

        return builder!!.create()
    }

    fun interface OnModelSelectedListener {
        fun onModelSelected(model: String)
    }

    fun setModelSelectedListener(listener: OnModelSelectedListener) {
        this.listener = listener
    }
}
