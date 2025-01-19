/**************************************************************************
 * Copyright (c) 2023-2025 Dmytro Ostapenko. All rights reserved.
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
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.teslasoft.assistant.R

class EditMessageDialogFragment : DialogFragment() {
    companion object {
        fun newInstance(prompt: String, position: Int) : EditMessageDialogFragment {
            val activationPromptDialogFragment = EditMessageDialogFragment()

            val args = Bundle()
            args.putString("prompt", prompt)
            args.putInt("position", position)

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
        return inflater.inflate(R.layout.fragment_message_edit, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        builder = MaterialAlertDialogBuilder(this.requireContext(), R.style.App_MaterialAlertDialog)

        val view: View = this.layoutInflater.inflate(R.layout.fragment_message_edit, null)

        promptInput = view.findViewById(R.id.prompt_input)
        promptInput?.setText(requireArguments().getString("prompt"))

        builder!!.setView(view)
            .setCancelable(false)
            .setPositiveButton(R.string.btn_save) { _, _ -> listener!!.onEdit(promptInput?.text.toString(), requireArguments().getInt("position")) }
            .setNeutralButton(R.string.btn_delete) { _, _ -> run {
                MaterialAlertDialogBuilder(this.requireContext(), R.style.App_MaterialAlertDialog)
                    .setTitle(R.string.label_delete_message)
                    .setMessage(R.string.msg_delete_message)
                    .setPositiveButton(R.string.yes) { _, _ -> listener!!.onDelete(requireArguments().getInt("position")) }
                    .setNegativeButton(R.string.no) { _, _ ->  }
                    .show()
            }}
            .setNegativeButton(R.string.btn_cancel) { _, _ ->  }

        return builder!!.create()
    }

    fun setStateChangedListener(listener: StateChangesListener) {
        this.listener = listener
    }

    interface StateChangesListener {
        fun onEdit(prompt: String, position: Int)
        fun onDelete(position: Int)
    }
}
