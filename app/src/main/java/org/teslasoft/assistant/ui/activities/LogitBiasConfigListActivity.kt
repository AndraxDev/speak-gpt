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

package org.teslasoft.assistant.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.edit
import androidx.fragment.app.FragmentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.LogitBiasConfigPreferences
import org.teslasoft.assistant.preferences.Preferences
import org.teslasoft.assistant.ui.adapters.LogitBiasConfigItemAdapter
import org.teslasoft.assistant.ui.fragments.dialogs.EditLogitBiasConfigDialogFragment
import org.teslasoft.assistant.util.Hash

class LogitBiasConfigListActivity : FragmentActivity() {

    private var btnAdd: ExtendedFloatingActionButton? = null
    private var btnBack: ImageButton? = null
    private var btnHelp: ImageButton? = null
    private var activityTitle: TextView? = null
    private var listView: ListView? = null

    private var list: ArrayList<HashMap<String, String>> = arrayListOf()
    private var adapter: LogitBiasConfigItemAdapter? = null

    private var logitBiasConfigPreferences: LogitBiasConfigPreferences? = null

    private var onSelectListener: LogitBiasConfigItemAdapter.OnSelectListener = object : LogitBiasConfigItemAdapter.OnSelectListener {
        override fun onClick(position: Int) {
            val resultIntent = Intent()
            resultIntent.putExtra("configId", Hash.hash(list[position]["label"] ?: return))
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        override fun onLongClick(position: Int) {
            if (position > 0) {
                val dialog: EditLogitBiasConfigDialogFragment = EditLogitBiasConfigDialogFragment.newInstance(list[position]["label"] ?: return, position)
                dialog.setListener(editDialogListener)
                dialog.show(supportFragmentManager, "EditLogitBiasConfigDialogFragment")
            }
        }

        override fun onEditBiases(position: Int) {
            startActivity(Intent(this@LogitBiasConfigListActivity, LogitBiasConfigActivity::class.java).apply {
                putExtra("configId", Hash.hash(list[position]["label"] ?: return))
                putExtra("label", list[position]["label"] ?: return)
            })
        }
    }

    private var editDialogListener: EditLogitBiasConfigDialogFragment.StateChangesListener = object : EditLogitBiasConfigDialogFragment.StateChangesListener {
        override fun onAdd(label: String) {
            logitBiasConfigPreferences!!.addConfig(label)
            reloadList()
        }

        override fun onEdit(position: Int, label: String) {
            logitBiasConfigPreferences!!.editConfig(list[position]["label"] ?: return, label)
            reloadList()
        }

        override fun onDelete(position: Int, id: String) {
            logitBiasConfigPreferences!!.deleteConfig(id)
            getSharedPreferences("logit_bias_config_$id", Context.MODE_PRIVATE).edit { clear() }
            reloadList()
        }

        override fun onError(message: String, position: Int) {
            Toast.makeText(this@LogitBiasConfigListActivity, message, Toast.LENGTH_SHORT).show()
            if (position == -1) {
                val dialog: EditLogitBiasConfigDialogFragment = EditLogitBiasConfigDialogFragment.newInstance("", position)
                dialog.setListener(this)
                dialog.show(supportFragmentManager, "EditLogitBiasConfigDialogFragment")
            } else {
                val dialog: EditLogitBiasConfigDialogFragment = EditLogitBiasConfigDialogFragment.newInstance(list[position]["label"] ?: return, position)
                dialog.setListener(this)
                dialog.show(supportFragmentManager, "EditLogitBiasConfigDialogFragment")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_logit_bias_config_list)

        window.statusBarColor = getColor(R.color.accent_250)
        window.navigationBarColor = getColor(R.color.accent_250)

        btnAdd = findViewById(R.id.btn_add)
        btnBack = findViewById(R.id.btn_back)
        btnHelp = findViewById(R.id.btn_help)
        activityTitle = findViewById(R.id.activity_title)
        listView = findViewById(R.id.list_view)

        listView?.divider = null

        logitBiasConfigPreferences = LogitBiasConfigPreferences.getLogitBiasConfigPreferences(this)
        initialize()
    }

    private fun reloadList() {
        if (list == null) list = arrayListOf()
        list.clear()

        var li = arrayListOf<HashMap<String, String>>()

        if (li == null) li = arrayListOf() // FUCK

        li.add(hashMapOf("label" to "Disable this feature", "id" to ""))

        if (li == null) li = arrayListOf() // FUCK

        var tmp = logitBiasConfigPreferences!!.getAllConfigs()

        if (tmp == null) tmp = arrayListOf()

        for (i in tmp ?: arrayListOf()) {
            li.add(i)
        }

        list = if (li == null) { /* still fuck */
            arrayListOf(hashMapOf("label" to "Disable this feature", "id" to ""))
        } else {
            li
        }

        // R8 bug fix, another fuck
        if (list == null) list = arrayListOf()

        runOnUiThread {
            adapter = LogitBiasConfigItemAdapter(list, this)
            adapter!!.setOnSelectListener(onSelectListener)
            listView!!.adapter = adapter
            adapter!!.notifyDataSetChanged()
        }
    }

    private fun initialize() {
        reloadList()

        btnBack!!.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        btnAdd!!.setOnClickListener {
            val dialog: EditLogitBiasConfigDialogFragment = EditLogitBiasConfigDialogFragment.newInstance("", -1)
            dialog.setListener(editDialogListener)
            dialog.show(supportFragmentManager, "EditLogitBiasConfigDialogFragment")
        }

        btnHelp!!.setOnClickListener {
            MaterialAlertDialogBuilder(this, R.style.App_MaterialAlertDialog)
                .setTitle("Help")
                .setMessage("Logit Bias Config is a feature that allows you to set custom logit bias for specific tokens. Logit bias is a value that is added to the logit of the token. It can be used to increase or decrease the probability of the token in the output sequence. For example, if you set logit bias to 100, the token will be more likely to appear in the output sequence. If you set logit bias to -100, the token will be less likely to appear in the output sequence.")
                .setPositiveButton("Close") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }
}
