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

package org.teslasoft.assistant.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.teslasoft.assistant.ui.activities.MainActivity
import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.ApiEndpointPreferences
import org.teslasoft.assistant.preferences.Preferences
import org.teslasoft.assistant.preferences.dto.ApiEndpointObject
import org.teslasoft.assistant.util.Hash
import org.teslasoft.core.api.network.RequestNetwork

class ActivationActivity : FragmentActivity() {

    private var btnNext: MaterialButton? = null
    private var keyInput: EditText? = null
    private var hostInput: EditText? = null
    private var debugFeatures: ConstraintLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activation)

        btnNext = findViewById(R.id.btn_next)
        keyInput = findViewById(R.id.password)
        hostInput = findViewById(R.id.username)
        debugFeatures = findViewById(R.id.debug_features)
        debugFeatures?.visibility = ConstraintLayout.INVISIBLE

        btnNext?.setOnClickListener {
            if (keyInput?.text.toString().trim() == "") {
                Toast.makeText(this, "Please enter an API key", Toast.LENGTH_SHORT).show()
            } else if (hostInput?.text.toString().trim() == "") {
                Toast.makeText(this, "Please enter API endpoint", Toast.LENGTH_SHORT).show()
            } else {
                if (hostInput?.text.toString().trim() == "debug") {
                    val password = keyInput?.text.toString().trim()
                    val requestNetwork = RequestNetwork(this)
                    debugFeatures?.visibility = ConstraintLayout.VISIBLE
                    requestNetwork.startRequestNetwork("GET", "https://gpt.teslasoft.org/key?password=$password", "A", object : RequestNetwork.RequestListener {
                        override fun onResponse(tag: String, message: String) {
                            debugFeatures?.visibility = ConstraintLayout.INVISIBLE
                            if (message == "incorrect") {
                                MaterialAlertDialogBuilder(this@ActivationActivity)
                                    .setTitle("Error")
                                    .setMessage("Failed to activate developer mode: Invalid developer access key.")
                                    .setPositiveButton("Close") { dialog, _ ->
                                        dialog.dismiss()
                                    }
                                    .show()
                            } else {
                                val hostname = "https://api.openai.com/v1/"
                                val apiEndpointObject = ApiEndpointObject("Default", hostname, message)
                                val apiEndpointPreferences = ApiEndpointPreferences.getApiEndpointPreferences(this@ActivationActivity)
                                apiEndpointPreferences.setApiEndpoint(this@ActivationActivity, apiEndpointObject)
                                val gPreferences = Preferences.getPreferences(this@ActivationActivity, "")
                                gPreferences.setApiEndpointId(Hash.hash("Default"))
                                gPreferences.setApiKey(message, this@ActivationActivity)
                                gPreferences.setCustomHost(hostname)
                                startActivity(Intent(this@ActivationActivity, MainActivity::class.java).setAction(Intent.ACTION_VIEW))
                                finish()
                            }
                        }

                        override fun onErrorResponse(tag: String, message: String) {
                            runOnUiThread {
                                debugFeatures?.visibility = ConstraintLayout.INVISIBLE
                                MaterialAlertDialogBuilder(this@ActivationActivity)
                                    .setTitle("Error")
                                    .setMessage("Please check your connection and try again.")
                                    .setPositiveButton("Close") { dialog, _ ->
                                        dialog.dismiss()
                                    }
                                    .show()
                            }
                        }
                    })
                } else {
                    val apiEndpointObject = ApiEndpointObject("Default", hostInput?.text.toString(), keyInput?.text.toString())
                    val apiEndpointPreferences = ApiEndpointPreferences.getApiEndpointPreferences(this)
                    apiEndpointPreferences.setApiEndpoint(this, apiEndpointObject)
                    val gPreferences = Preferences.getPreferences(this, "")
                    gPreferences.setApiEndpointId(Hash.hash("Default"))
                    gPreferences.setApiKey(keyInput?.text.toString(), this)
                    gPreferences.setCustomHost(hostInput?.text.toString())
                    startActivity(Intent(this, MainActivity::class.java).setAction(Intent.ACTION_VIEW))
                    finish()
                }
            }
        }
    }
}
