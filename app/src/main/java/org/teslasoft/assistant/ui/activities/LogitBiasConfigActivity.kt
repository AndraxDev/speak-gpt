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

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.LogitBiasPreferences
import org.teslasoft.assistant.preferences.Preferences
import org.teslasoft.assistant.ui.adapters.LogitBiasItemAdapter

class LogitBiasConfigActivity : FragmentActivity() {

    private var listView: ListView? = null
    private var btnBack: ImageButton? = null
    private var btnHelp: ImageButton? = null
    private var activityTitle: TextView? = null
    private var fieldToken: TextInputEditText? = null
    private var fieldTokenId: TextInputEditText? = null
    private var fieldLogitBias: TextInputEditText? = null
    private var btnAddPair: MaterialButton? = null

    private var list: ArrayList<HashMap<String, String>> = arrayListOf()
    private var adapter: LogitBiasItemAdapter? = null

    private var preferences: Preferences? = null
    private var logitBiasPreferences: LogitBiasPreferences? = null

    private var onSelectListener: LogitBiasItemAdapter.OnSelectListener = object : LogitBiasItemAdapter.OnSelectListener {
        override fun onClick(position: Int) { /* unused */ }

        override fun onLongClick(position: Int) { /* unused */ }

        override fun onDelete(position: Int) {
            logitBiasPreferences!!.removeLogitBias(list[position]["tokenId"] ?: return)
            reloadList()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_logit_bias_list)

        window.statusBarColor = getColor(R.color.accent_250)
        window.navigationBarColor = getColor(R.color.accent_250)

        listView = findViewById(R.id.list_view)
        btnBack = findViewById(R.id.btn_back)
        btnHelp = findViewById(R.id.btn_help)
        activityTitle = findViewById(R.id.activity_title)
        fieldToken = findViewById(R.id.field_token)
        fieldTokenId = findViewById(R.id.field_token_id)
        fieldLogitBias = findViewById(R.id.field_logit_bias)
        btnAddPair = findViewById(R.id.btn_add_pair)

        listView?.divider = null

        val extras = intent.extras

        if (extras != null) {
            val configId = extras.getString("configId")

            if (configId != null) {
                logitBiasPreferences = LogitBiasPreferences(this, configId)
                initialize()
            } else {
                finish()
            }
        } else {
            finish()
        }
    }

    private fun reloadList() {
        val logitBiasList = logitBiasPreferences!!.getLogitBiasesList()

        for (i in logitBiasList) {
            val map = HashMap<String, String>()
            map["tokenId"] = i.tokenId
            map["logitBias"] = i.logitBias
            list.add(map)
        }

        // R8 bug fix
        if (list == null) list = arrayListOf()

        runOnUiThread {
            adapter = LogitBiasItemAdapter(list, this)
            adapter!!.setOnSelectListener(onSelectListener)
            listView!!.adapter = adapter
            adapter!!.notifyDataSetChanged()
        }
    }

    private fun initialize() {
        reloadList()

        btnBack!!.setOnClickListener {
            finish()
        }

        btnHelp!!.setOnClickListener {
            // Show help dialog
        }
    }
}