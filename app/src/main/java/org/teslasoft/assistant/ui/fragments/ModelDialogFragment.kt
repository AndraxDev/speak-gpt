/**************************************************************************
 * Copyright (c) 2023 Dmytro Ostapenko. All rights reserved.
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

package org.teslasoft.assistant.ui.fragments

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.Preferences

class ModelDialogFragment : DialogFragment() {
    companion object {
        fun newInstance(name: String) : ModelDialogFragment {
            val modelDialogFragment = ModelDialogFragment()

            val args = Bundle()
            args.putString("name", name)

            modelDialogFragment.arguments = args

            return modelDialogFragment
        }
    }

    private var builder: AlertDialog.Builder? = null

    private var context: Context? = null

    private var gpt_35_turbo: RadioButton? = null
    private var gpt_35_turbo_0301: RadioButton? = null
    private var gpt_4: RadioButton? = null
    private var gpt_4_0314: RadioButton? = null
    private var gpt_4_32k: RadioButton? = null
    private var gpt_4_32k_0314: RadioButton? = null
    private var text_davinci_003: RadioButton? = null
    private var text_davinci_002: RadioButton? = null
    private var text_curie_001: RadioButton? = null
    private var text_babbage_001: RadioButton? = null
    private var text_ada_001: RadioButton? = null
    private var davinci: RadioButton? = null
    private var curie: RadioButton? = null
    private var babbage: RadioButton? = null
    private var ada: RadioButton? = null
    private var ft: RadioButton? = null
    private var ftInput: EditText? = null
    private var maxTokens: EditText? = null
    private var endSeparator: EditText? = null

    private var listener: StateChangesListener? = null

    private var model = "gpt-3.5-turbo"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this.activity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_model, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        builder = MaterialAlertDialogBuilder(this.requireContext())

        val view: View = this.layoutInflater.inflate(R.layout.fragment_model, null)

        gpt_35_turbo = view.findViewById(R.id.gpt_35_turbo)
        gpt_35_turbo_0301 = view.findViewById(R.id.gpt_35_turbo_0301)
        gpt_4 = view.findViewById(R.id.gpt_4)
        gpt_4_0314 = view.findViewById(R.id.gpt_4_0314)
        gpt_4_32k = view.findViewById(R.id.gpt_4_32k)
        gpt_4_32k_0314 = view.findViewById(R.id.gpt_4_32k_0314)
        text_davinci_003 = view.findViewById(R.id.text_davinci_003)
        text_davinci_002 = view.findViewById(R.id.text_davinci_002)
        text_curie_001 = view.findViewById(R.id.text_curie_001)
        text_babbage_001 = view.findViewById(R.id.text_babbage_001)
        text_ada_001 = view.findViewById(R.id.text_ada_001)
        davinci = view.findViewById(R.id.davinci)
        curie = view.findViewById(R.id.curie)
        babbage = view.findViewById(R.id.babbage)
        ada = view.findViewById(R.id.ada)
        ft = view.findViewById(R.id.ft)
        ftInput = view.findViewById(R.id.ft_input)
        maxTokens = view.findViewById(R.id.max_tokens)
        endSeparator = view.findViewById(R.id.end_separator)

        maxTokens?.setText(Preferences.getPreferences(requireActivity()).getMaxTokens().toString())
        endSeparator?.setText(Preferences.getPreferences(requireActivity()).getEndSeparator())

        gpt_35_turbo?.setOnClickListener { model = "gpt-3.5-turbo" }
        gpt_35_turbo_0301?.setOnClickListener { model = "gpt-3.5-turbo-0301" }
        gpt_4?.setOnClickListener { model = "gpt-4" }
        gpt_4_0314?.setOnClickListener { model = "gpt-4-0314" }
        gpt_4_32k?.setOnClickListener { model = "gpt-4-32k" }
        gpt_4_32k_0314?.setOnClickListener { model = "gpt-4-32k-0314" }
        text_davinci_003?.setOnClickListener { model = "text-davinci-003" }
        text_davinci_002?.setOnClickListener { model = "text-davinci-002" }
        text_curie_001?.setOnClickListener { model = "text-curie-001" }
        text_babbage_001?.setOnClickListener { model = "text-babbage-001" }
        text_ada_001?.setOnClickListener { model = "text-ada-001" }
        davinci?.setOnClickListener { model = "davinci" }
        curie?.setOnClickListener { model = "curie" }
        babbage?.setOnClickListener { model = "babbage" }
        ada?.setOnClickListener { model = "ada" }
        ft?.setOnClickListener { model = ftInput?.text.toString() }

        ftInput?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                /* unused */
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                model = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {
                /* unused */
            }
        })

        builder!!.setView(view)
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ -> validateForm() }
            .setNegativeButton("Cancel") { _, _ ->  }

        when (requireArguments().getString("name")) { // load default model if settings not found
            "gpt-3.5-turbo" -> gpt_35_turbo?.isChecked = true
            "gpt-3.5-turbo-0301" -> gpt_35_turbo_0301?.isChecked = true
            "gpt-4" -> gpt_4?.isChecked = true
            "gpt-4-0314" -> gpt_4_0314?.isChecked = true
            "gpt-4-32k" -> gpt_4_32k?.isChecked = true
            "gpt-4-32k-0314" -> gpt_4_32k_0314?.isChecked = true
            "text-davinci-003" -> text_davinci_003?.isChecked = true
            "text-davinci-002" -> text_davinci_002?.isChecked = true
            "text-curie-001" -> text_curie_001?.isChecked = true
            "text-babbage-001" -> text_babbage_001?.isChecked = true
            "text-ada-001" -> text_ada_001?.isChecked = true
            "davinci" -> davinci?.isChecked = true
            "curie" -> curie?.isChecked = true
            "babbage" -> babbage?.isChecked = true
            "ada" -> ada?.isChecked = true
            else -> {
                ft?.isChecked = true
                ftInput?.setText(requireArguments().getString("name"))
            }
        }

        return builder!!.create()
    }

    private fun validateForm() {
        if (ftInput?.text.toString() == "") {
            listener!!.onFormError(model, maxTokens?.text.toString(), endSeparator?.text.toString())
            return
        }

        if (maxTokens?.text.toString() == "") {
            listener!!.onFormError(model, maxTokens?.text.toString(), endSeparator?.text.toString())
            return
        }

        if (maxTokens?.text.toString().toInt() > 2048 && !model.contains("gpt-4")) {
            listener!!.onFormError(model, maxTokens?.text.toString(), endSeparator?.text.toString())
            return
        }

        if (maxTokens?.text.toString().toInt() > 8192 && model.contains("gpt-4")) {
            listener!!.onFormError(model, maxTokens?.text.toString(), endSeparator?.text.toString())
            return
        }

        listener!!.onSelected(model, maxTokens?.text.toString(), endSeparator?.text.toString())
    }

    fun setStateChangedListener(listener: StateChangesListener) {
        this.listener = listener
    }

    public interface StateChangesListener {
        fun onSelected(name: String, maxTokens: String, endSeparator: String)
        fun onFormError(name: String, maxTokens: String, endSeparator: String)
    }
}