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

package org.teslasoft.assistant.ui.onboarding

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Process
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.Toast

import androidx.fragment.app.FragmentActivity

import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

import org.teslasoft.assistant.ui.activities.MainActivity
import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.GlobalPreferences
import org.teslasoft.assistant.preferences.Preferences

class ActivationActivity : FragmentActivity() {

    private var btnNext: MaterialButton? = null
    private var keyInput: EditText? = null
    private var hostInput: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activation)

        btnNext = findViewById(R.id.btn_next)
        keyInput = findViewById(R.id.key_input)
        hostInput = findViewById(R.id.host_input)

        val processName = getProcessName(this)
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                WebView.setDataDirectorySuffix(processName!!)
            }
        } catch (ignored: Exception) { /* unused */ }

        btnNext?.setOnClickListener {
            if (keyInput?.text.toString() == "") {
                Toast.makeText(this, "Please enter an API key", Toast.LENGTH_SHORT).show()
            } else if (hostInput?.text.toString() == "") {
                Toast.makeText(this, "Please enter API endpoint", Toast.LENGTH_SHORT).show()
            } else {
                val gPreferences = GlobalPreferences.getPreferences(this)
                gPreferences.setApiKey(keyInput?.text.toString(), this)
                gPreferences.setCustomHost(hostInput?.text.toString())
                startActivity(Intent(this, MainActivity::class.java).setAction(Intent.ACTION_VIEW))
                finish()
            }
        }
    }

    private fun getProcessName(context: Context?): String? {
        if (context == null) return null
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (processInfo in manager.runningAppProcesses) {
            if (processInfo.pid == Process.myPid()) return processInfo.processName
        }
        return null
    }
}
