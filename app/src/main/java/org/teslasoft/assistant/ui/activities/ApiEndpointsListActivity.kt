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
import org.teslasoft.assistant.preferences.ApiEndpointPreferences
import org.teslasoft.assistant.preferences.Preferences
import org.teslasoft.assistant.preferences.dto.ApiEndpointObject
import org.teslasoft.assistant.ui.adapters.ApiEndpointListItemAdapter
import org.teslasoft.assistant.ui.fragments.dialogs.EditApiEndpointDialogFragment
import org.teslasoft.assistant.util.Hash

class ApiEndpointsListActivity : FragmentActivity() {

    private var btnAdd: ExtendedFloatingActionButton? = null
    private var btnBack: ImageButton? = null
    private var activityTitle: TextView? = null
    private var listView: ListView? = null

    private var list: ArrayList<HashMap<String, String>> = arrayListOf()
    private var adapter: ApiEndpointListItemAdapter? = null

    private var preferences: Preferences? = null
    private var apiEndpointPreferences: ApiEndpointPreferences? = null

    private var onSelectListener: ApiEndpointListItemAdapter.OnSelectListener = object : ApiEndpointListItemAdapter.OnSelectListener {
        override fun onClick(position: Int) {
            val resultIntent = Intent()
            resultIntent.putExtra("apiEndpointId", Hash.hash(list[position]["label"] ?: return))
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        override fun onLongClick(position: Int) {
            val dialog: EditApiEndpointDialogFragment = EditApiEndpointDialogFragment.newInstance(list[position]["label"] ?: return, list[position]["host"] ?: return, list[position]["apiKey"] ?: return, position)
            dialog.setListener(editDialogListener)
            dialog.show(supportFragmentManager, "EditApiEndpointDialogFragment")
        }
    }

    private var editDialogListener: EditApiEndpointDialogFragment.StateChangesListener = object : EditApiEndpointDialogFragment.StateChangesListener {
        override fun onAdd(apiEndpoint: ApiEndpointObject) {
            apiEndpointPreferences!!.setApiEndpoint(this@ApiEndpointsListActivity, apiEndpoint)
            reloadList()
        }

        override fun onEdit(apiEndpoint: ApiEndpointObject, position: Int) {
            apiEndpointPreferences!!.editEndpoint(this@ApiEndpointsListActivity, list[position]["label"]?: return, apiEndpoint)
            reloadList()
        }

        override fun onDelete(position: Int, id: String) {
            if (position > 0) {
                apiEndpointPreferences!!.deleteApiEndpoint(this@ApiEndpointsListActivity, id)
                reloadList()
            } else {
                Toast.makeText(this@ApiEndpointsListActivity, "Default API endpoint can not be deleted", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onError(message: String, position: Int) {
            Toast.makeText(this@ApiEndpointsListActivity, message, Toast.LENGTH_SHORT).show()
            if (position == -1) {
                val dialog: EditApiEndpointDialogFragment = EditApiEndpointDialogFragment.newInstance("", "", "", position)
                dialog.setListener(this)
                dialog.show(supportFragmentManager, "EditApiEndpointDialogFragment")
            } else {
                val dialog: EditApiEndpointDialogFragment = EditApiEndpointDialogFragment.newInstance(list[position]["label"] ?: return, list[position]["host"] ?: return, list[position]["apiKey"] ?: return, position)
                dialog.setListener(this)
                dialog.show(supportFragmentManager, "EditApiEndpointDialogFragment")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_api_endpoint_list)

        window.statusBarColor = getColor(R.color.accent_250)
        window.navigationBarColor = getColor(R.color.accent_250)

        btnAdd = findViewById(R.id.btn_add)
        btnBack = findViewById(R.id.btn_back)
        activityTitle = findViewById(R.id.activity_title)
        listView = findViewById(R.id.list_view)

        listView?.divider = null

        apiEndpointPreferences = ApiEndpointPreferences.getApiEndpointPreferences(this)
        initialize()
    }

    private fun reloadList() {
        list.clear()
        val apiList = apiEndpointPreferences!!.getApiEndpointsList(this)

        for (i in apiList) {
            val map = HashMap<String, String>()
            map["label"] = i.label
            map["host"] = i.host
            map["apiKey"] = i.apiKey
            list.add(map)
        }

        // R8 bug fix
        if (list == null) list = arrayListOf()

        runOnUiThread {
            adapter = ApiEndpointListItemAdapter(list, this)
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
            val dialog: EditApiEndpointDialogFragment = EditApiEndpointDialogFragment.newInstance("", "", "", -1)
            dialog.setListener(editDialogListener)
            dialog.show(supportFragmentManager, "EditApiEndpointDialogFragment")
        }
    }
}