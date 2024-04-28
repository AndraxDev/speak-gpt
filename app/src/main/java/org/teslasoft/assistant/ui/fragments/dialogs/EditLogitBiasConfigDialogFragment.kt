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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import org.teslasoft.assistant.R
import org.teslasoft.assistant.util.Hash

class EditLogitBiasConfigDialogFragment : DialogFragment() {

    companion object {
        fun newInstance(label: String, position: Int) : EditLogitBiasConfigDialogFragment {
            val editLogitBiasConfigDialogFragment = EditLogitBiasConfigDialogFragment()

            val args = Bundle()
            args.putString("label", label)
            args.putInt("position", position)

            editLogitBiasConfigDialogFragment.arguments = args

            return editLogitBiasConfigDialogFragment
        }
    }

    private var textDialogTitle: TextView? = null
    private var fieldLogitBiasLabel: TextInputEditText? = null

    private var builder: AlertDialog.Builder? = null

    private var listener: StateChangesListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_edit_bias_config, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        builder = MaterialAlertDialogBuilder(this.requireContext())

        val view: View = this.layoutInflater.inflate(R.layout.fragment_edit_bias_config, null)

        textDialogTitle = view.findViewById(R.id.text_dialog_title)
        fieldLogitBiasLabel = view.findViewById(R.id.field_logit_bias_label)

        if (requireArguments().getString("label") != null) {
            fieldLogitBiasLabel?.setText(requireArguments().getString("label"))
        }

        builder!!.setView(view)
            .setCancelable(false)
            .setPositiveButton("Save") { _, _ -> validateForm() }
            .setNeutralButton("Delete") { _, _ -> run {
                MaterialAlertDialogBuilder(this.requireContext())
                    .setTitle("Delete config")
                    .setMessage("Are you sure you want to delete this config?")
                    .setPositiveButton("Yes") { _, _ -> listener!!.onDelete(requireArguments().getInt("position"), Hash.hash(requireArguments().getString("label")!!)) }
                    .setNegativeButton("No") { _, _ ->  }
                    .show()
            } }
            .setNegativeButton("Cancel") { _, _ ->  }

        return builder!!.create()
    }

    fun validateForm() {
        if (fieldLogitBiasLabel?.text.toString().isEmpty()) {
            listener!!.onError("Label be empty", requireArguments().getInt("position"))
        } else {
            if (requireArguments().getString("label") == "") {
                listener!!.onAdd(fieldLogitBiasLabel?.text.toString())
            } else {
                listener!!.onEdit(requireArguments().getInt("position"), fieldLogitBiasLabel?.text.toString())
            }
        }
    }

    fun setListener(listener: StateChangesListener) {
        this.listener = listener
    }

    interface StateChangesListener {
        fun onAdd(label: String)
        fun onEdit(position: Int, label: String)
        fun onDelete(position: Int, id: String)
        fun onError(message: String, position: Int)
    }
}