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
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

import com.google.android.material.dialog.MaterialAlertDialogBuilder

import org.teslasoft.assistant.R

class ActivationPromptDialogFragment : DialogFragment() {
    companion object {
        fun newInstance(prompt: String) : ActivationPromptDialogFragment {
            val activationPromptDialogFragment = ActivationPromptDialogFragment()

            val args = Bundle()
            args.putString("prompt", prompt)

            activationPromptDialogFragment.arguments = args

            return activationPromptDialogFragment
        }
    }

    private var builder: AlertDialog.Builder? = null

    private var context: Context? = null

    private var promptInput: EditText? = null

    private var listener: StateChangesListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this.activity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_activation_prompt, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        builder = MaterialAlertDialogBuilder(this.requireContext())

        val view: View = this.layoutInflater.inflate(R.layout.fragment_activation_prompt, null)

        promptInput = view.findViewById(R.id.prompt_input)
        promptInput?.setText(requireArguments().getString("prompt"))

        /*promptInput?.requestFocus()

        promptInput?.setOnKeyListener { v, keyCode, event ->
            run {
                if (event.action == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE)) {
                    dismiss()
                    return@run true
                }
                return@run false
            }
        }*/

        builder!!.setView(view)
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ -> validateForm() }
            .setNegativeButton("Cancel") { _, _ ->  }

        return builder!!.create()
    }

    private fun validateForm() {
        listener!!.onEdit(promptInput?.text.toString())
    }

    fun setStateChangedListener(listener: StateChangesListener) {
        this.listener = listener
    }

    fun interface StateChangesListener {
        fun onEdit(prompt: String)
    }
}
