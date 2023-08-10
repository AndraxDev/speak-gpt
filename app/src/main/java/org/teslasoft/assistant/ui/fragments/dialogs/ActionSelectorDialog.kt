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

package org.teslasoft.assistant.ui.fragments.dialogs

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

import com.google.android.material.dialog.MaterialAlertDialogBuilder

import org.teslasoft.assistant.R

class ActionSelectorDialog : DialogFragment() {
    companion object {
        fun newInstance(prompt: String) : ActionSelectorDialog {
            val actionSelectorDialog = ActionSelectorDialog()

            val args = Bundle()
            args.putString("text", prompt)

            actionSelectorDialog.arguments = args

            return actionSelectorDialog
        }
    }

    private var builder: AlertDialog.Builder? = null

    private var context: Context? = null

    private var listener: StateChangesListener? = null

    private var btnPrompt: Button? = null

    private var btnExplain: Button? = null

    private var btnImage: Button? = null

    private var btnCancel: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this.activity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_action_selector, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        builder = MaterialAlertDialogBuilder(this.requireContext())

        val view: View = this.layoutInflater.inflate(R.layout.fragment_action_selector, null)

        btnPrompt = view.findViewById(R.id.btnPrompt)
        btnExplain = view.findViewById(R.id.btnExplain)
        btnImage = view.findViewById(R.id.btnImage)
        btnCancel = view.findViewById(R.id.btnCancel)

        btnPrompt?.setOnClickListener {
            listener!!.onSelected("prompt", arguments?.getString("text")?:"")
            this.dismiss()
        }

        btnExplain?.setOnClickListener {
            listener!!.onSelected("explain", arguments?.getString("text")?:"")
            this.dismiss()
        }

        btnImage?.setOnClickListener {
            listener!!.onSelected("image", arguments?.getString("text")?:"")
            this.dismiss()
        }

        btnCancel?.setOnClickListener {
            listener!!.onSelected("cancel", arguments?.getString("text")?:"")
            this.dismiss()
        }

        builder!!.setView(view)
            .setCancelable(false)

        return builder!!.create()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)

        listener!!.onSelected("cancel", arguments?.getString("text")?:"")
    }

    fun setStateChangedListener(listener: StateChangesListener) {
        this.listener = listener
    }

    fun interface StateChangesListener {
        fun onSelected(type: String, text: String)
    }
}
