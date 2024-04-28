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

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
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
    private var activityTitle: TextView? = null
    private var listView: ListView? = null

    private var list: ArrayList<HashMap<String, String>> = arrayListOf()
    private var adapter: LogitBiasConfigItemAdapter? = null

    private var preferences: Preferences? = null
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
        activityTitle = findViewById(R.id.activity_title)
        listView = findViewById(R.id.list_view)

        listView?.divider = null

        logitBiasConfigPreferences = LogitBiasConfigPreferences.getLogitBiasConfigPreferences(this)
        initialize()
    }

    private fun reloadList() {
        list.clear()

        val li = ArrayList<HashMap<String, String>>()

        li.add(hashMapOf("label" to "Disable this feature", "id" to ""))

        for (i in logitBiasConfigPreferences!!.getAllConfigs()) {
            li.add(i)
        }

        list = if (li == null) {
            arrayListOf(hashMapOf("label" to "Disable this feature", "id" to ""))
        } else {
            li
        }

        // R8 bug fix
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
    }
}
