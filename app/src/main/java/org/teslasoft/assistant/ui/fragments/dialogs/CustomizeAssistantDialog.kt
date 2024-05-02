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
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import org.teslasoft.assistant.R

class CustomizeAssistantDialog : DialogFragment() {
    companion object {
        fun newInstance(isEdit: Boolean, name: String, avatarType: String, avatarId: String) : CustomizeAssistantDialog {
            val customizeAssistantDialog = CustomizeAssistantDialog()

            val args = Bundle()
            args.putBoolean("isEdit", isEdit)
            args.putString("name", name)
            args.putString("avatarType", avatarType)
            args.putString("avatarId", avatarId)

            customizeAssistantDialog.arguments = args

            return customizeAssistantDialog
        }
    }

    private var builder: AlertDialog.Builder? = null

    private var textDialogTitle: TextView? = null
    private var fieldAssistantName: TextInputEditText? = null
    private var btnView1: ImageButton? = null
    private var btnView2: ImageButton? = null
    private var btnView3: ImageButton? = null
    private var btnView4: ImageButton? = null
    private var btnView5: ImageButton? = null
    private var btnSelectFile: MaterialButton? = null
    private var previewFile: ImageView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_customize, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        builder = MaterialAlertDialogBuilder(this.requireContext(), R.style.App_MaterialAlertDialog)

        val view: View = this.layoutInflater.inflate(R.layout.fragment_customize, null)

        textDialogTitle = view.findViewById(R.id.text_dialog_title_cust)
        fieldAssistantName = view.findViewById(R.id.field_assistant_name)
        btnView1 = view.findViewById(R.id.view1)
        btnView2 = view.findViewById(R.id.view2)
        btnView3 = view.findViewById(R.id.view3)
        btnView4 = view.findViewById(R.id.view4)
        btnView5 = view.findViewById(R.id.view5)
        btnSelectFile = view.findViewById(R.id.btn_file_select)
        previewFile = view.findViewById(R.id.current_avatar_cust)

        builder!!.setView(view)
            .setCancelable(false)
            .setPositiveButton("Save") { _, _ ->  }
            .setNegativeButton("Cancel") { _, _ ->  }

        return builder!!.create()
    }
}