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

package org.teslasoft.assistant.ui.activities

import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginRight
import androidx.fragment.app.FragmentActivity
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.ApiEndpointPreferences
import org.teslasoft.assistant.preferences.Preferences
import org.teslasoft.assistant.preferences.dto.ApiEndpointObject
import org.teslasoft.assistant.theme.ThemeManager
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

    private var apiEndpointPreferences: ApiEndpointPreferences? = null

    private var actionBar: ConstraintLayout? = null

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

        override fun onEdit(oldLabel: String, apiEndpoint: ApiEndpointObject, position: Int) {
            if (oldLabel == "Default" && apiEndpoint.label != "Default") {
                Toast.makeText(this@ApiEndpointsListActivity, getString(R.string.default_api_endpoint_error), Toast.LENGTH_SHORT).show()
                return
            }
            apiEndpointPreferences!!.editEndpoint(this@ApiEndpointsListActivity, list[position]["label"]?: return, apiEndpoint)
            reloadList()
        }

        override fun onDelete(position: Int, id: String) {
            if (list != null && position >= 0 && list[position]["label"] == "Default") {
                Toast.makeText(this@ApiEndpointsListActivity, getString(R.string.default_api_endpoint_error_delete), Toast.LENGTH_SHORT).show()
            } else if (/* R8 fucker */ list != null && list.size > 1) {
                apiEndpointPreferences!!.deleteApiEndpoint(this@ApiEndpointsListActivity, id)
                reloadList()
            } else {
                Toast.makeText(this@ApiEndpointsListActivity, getString(R.string.api_endpoint_error_zero), Toast.LENGTH_SHORT).show()
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

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_api_endpoint_list)

        btnAdd = findViewById(R.id.btn_add)
        btnBack = findViewById(R.id.btn_back)
        activityTitle = findViewById(R.id.activity_title)
        listView = findViewById(R.id.list_view)
        actionBar = findViewById(R.id.action_bar)

        val preferences = Preferences.getPreferences(this, "")

        ThemeManager.getThemeManager().applyTheme(this, isDarkThemeEnabled() && preferences.getAmoledPitchBlack())

        if (isDarkThemeEnabled() && preferences.getAmoledPitchBlack()) {
            window.setBackgroundDrawableResource(R.color.amoled_window_background)

            if (android.os.Build.VERSION.SDK_INT <= 34) {
                window.navigationBarColor = ResourcesCompat.getColor(resources, R.color.amoled_window_background, theme)
                window.statusBarColor = ResourcesCompat.getColor(resources, R.color.amoled_accent_50, theme)
            }

            actionBar?.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.amoled_accent_50, theme))
        } else {
            val colorDrawable = ColorDrawable(SurfaceColors.SURFACE_0.getColor(this))
            window.setBackgroundDrawable(colorDrawable)

            if (android.os.Build.VERSION.SDK_INT <= 34) {
                window.navigationBarColor = SurfaceColors.SURFACE_0.getColor(this)
                window.statusBarColor = SurfaceColors.SURFACE_4.getColor(this)
            }

            actionBar?.setBackgroundColor(SurfaceColors.SURFACE_4.getColor(this))
        }

        listView?.divider = null

        apiEndpointPreferences = ApiEndpointPreferences.getApiEndpointPreferences(this)
        initialize()
    }

    private fun reloadList() {
        if (list == null) list = arrayListOf()

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

    private fun isDarkThemeEnabled(): Boolean {
        return when (resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            Configuration.UI_MODE_NIGHT_NO -> false
            Configuration.UI_MODE_NIGHT_UNDEFINED -> false
            else -> false
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

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        adjustPaddings()
    }

    private fun adjustPaddings() {
        if (Build.VERSION.SDK_INT < 35) return
        try {
            val actionBar = findViewById<ConstraintLayout>(R.id.action_bar)
            actionBar?.setPadding(
                0,
                window.decorView.rootWindowInsets.getInsets(WindowInsets.Type.statusBars()).top,
                0,
                0
            )

            val list = findViewById<ListView>(R.id.list_view)
            list?.setPadding(
                0,
                0,
                0,
                window.decorView.rootWindowInsets.getInsets(WindowInsets.Type.navigationBars()).bottom
            )

            val extendedFab = findViewById<ExtendedFloatingActionButton>(R.id.btn_add)
            val params: ConstraintLayout.LayoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, extendedFab!!.marginRight, window.decorView.rootWindowInsets.getInsets(WindowInsets.Type.navigationBars()).bottom + extendedFab!!.marginBottom)
            params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            extendedFab.layoutParams = params
        } catch (_: Exception) { /* unused */ }
    }
}