/**************************************************************************
 * Copyright (c) 2023-2026 Dmytro Ostapenko. All rights reserved.
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

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.aallam.ktoken.Encoding
import com.aallam.ktoken.Tokenizer
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.LogitBiasPreferences
import org.teslasoft.assistant.preferences.dto.LogitBiasObject
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
    private var logitBiasLoader: CircularProgressIndicator? = null

    private var list: ArrayList<HashMap<String, String>> = arrayListOf()
    private var adapter: LogitBiasItemAdapter? = null

    private var logitBiasPreferences: LogitBiasPreferences? = null

    private var onSelectListener: LogitBiasItemAdapter.OnSelectListener = object : LogitBiasItemAdapter.OnSelectListener {
        override fun onClick(position: Int) {
            fieldLogitBias!!.setText(list[position]["logitBias"].toString())
            fieldTokenId!!.setText(list[position]["tokenId"].toString())
        }

        override fun onLongClick(position: Int) {
            fieldLogitBias!!.setText(list[position]["logitBias"].toString())
            fieldTokenId!!.setText(list[position]["tokenId"].toString())
        }

        override fun onDelete(position: Int) {
            logitBiasPreferences!!.removeLogitBias(list[position]["tokenId"] ?: return)
            list.clear()
            reloadList()
        }

    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_logit_bias_list)

        listView = findViewById(R.id.list_view)
        btnBack = findViewById(R.id.btn_back)
        btnHelp = findViewById(R.id.btn_help)
        activityTitle = findViewById(R.id.activity_title)
        fieldToken = findViewById(R.id.field_token)
        fieldTokenId = findViewById(R.id.field_token_id)
        fieldLogitBias = findViewById(R.id.field_logit_bias)
        btnAddPair = findViewById(R.id.btn_add_pair)
        logitBiasLoader = findViewById(R.id.logit_bias_loading)

        listView?.divider = null

        btnAddPair!!.setOnClickListener {
            if (fieldLogitBias!!.text.toString() == "" || fieldLogitBias!!.text.toString() == "-" || fieldLogitBias!!.text.toString() == "e") {
                Toast.makeText(this, getString(R.string.logit_bias_error_empty), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (fieldTokenId!!.text.toString() != "" && fieldTokenId!!.text.toString() != "-" && fieldTokenId!!.text.toString() != "e") {
                val tokenId = fieldTokenId!!.text.toString()

                val logitBias = fieldLogitBias!!.text.toString()

                if (logitBias.toInt() > 100 || logitBias.toInt() < -100) {
                    Toast.makeText(this, getString(R.string.logit_bias_error_range), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (tokenId.isNotEmpty() && logitBias.isNotEmpty()) {
                    logitBiasPreferences!!.setLogitBias(LogitBiasObject(tokenId, logitBias))
                    list.clear()
                    reloadList()
                    fieldLogitBias!!.setText("")
                    fieldTokenId!!.setText("")
                    fieldToken!!.setText("")
                }
            } else if (fieldToken!!.text.toString() != "") {
                CoroutineScope(Dispatchers.Main).launch {
                    val tokenizer = Tokenizer.of(encoding = Encoding.CL100K_BASE)
                    val tokens = tokenizer.encode(fieldToken!!.text.toString())
                    val logitBias = fieldLogitBias!!.text.toString()

                    for (i in tokens) {
                        val tokenId = i.toString()

                        if (logitBias.toInt() > 100 || logitBias.toInt() < -100) {
                            Toast.makeText(this@LogitBiasConfigActivity, getString(R.string.logit_bias_error_range), Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        if (tokenId.isNotEmpty() && logitBias.isNotEmpty()) {
                            logitBiasPreferences!!.setLogitBias(LogitBiasObject(tokenId, logitBias))
                            list.clear()
                            reloadList()
                            fieldLogitBias!!.setText("")
                            fieldTokenId!!.setText("")
                            fieldToken!!.setText("")
                        }
                    }
                }
            } else {
                Toast.makeText(this, getString(R.string.logit_bias_error_token_id), Toast.LENGTH_SHORT).show()
            }
        }

        val extras = intent.extras

        if (extras != null) {
            val configId = extras.getString("configId")

            if (configId != null) {
                logitBiasPreferences = LogitBiasPreferences(this, configId)
                activityTitle!!.text = extras.getString("label") ?: getString(R.string.logit_bias_config)
                initialize()
            } else {
                finish()
            }
        } else {
            finish()
        }
    }

    private fun reloadList() {
        runOnUiThread {
            logitBiasLoader?.visibility = View.VISIBLE
            listView?.visibility = ListView.GONE
            btnAddPair?.isEnabled = false
        }

        if (list == null) list = arrayListOf()

        var logitBiasList = logitBiasPreferences!!.getLogitBiasesList()

        if (logitBiasList == null) logitBiasList = arrayListOf()

        CoroutineScope(Dispatchers.Main).launch {
            val tokenizer = Tokenizer.of(encoding = Encoding.CL100K_BASE)

            for (i in logitBiasList) {
                val map = HashMap<String, String>()
                map["tokenId"] = i.tokenId
                map["logitBias"] = i.logitBias
                map["tokenContent"] = tokenizer.decode(i.tokenId.toString().toInt())
                list.add(map)
            }

            // R8 bug fix
            if (list == null) list = arrayListOf()

            runOnUiThread {
                adapter = LogitBiasItemAdapter(list, this@LogitBiasConfigActivity)
                adapter!!.setOnSelectListener(onSelectListener)
                listView!!.adapter = adapter
                adapter!!.notifyDataSetChanged()

                logitBiasLoader?.visibility = View.GONE
                listView?.visibility = ListView.VISIBLE
                btnAddPair?.isEnabled = true
            }
        }
    }

    private fun initialize() {
        reloadList()

        btnBack!!.setOnClickListener {
            finish()
        }

        btnHelp!!.setOnClickListener {
            MaterialAlertDialogBuilder(this, R.style.App_MaterialAlertDialog)
                .setTitle(R.string.help)
                .setMessage(R.string.logit_bias_help)
                .setPositiveButton(R.string.btn_close) { _, _ -> }
                .show()
        }
    }
}
