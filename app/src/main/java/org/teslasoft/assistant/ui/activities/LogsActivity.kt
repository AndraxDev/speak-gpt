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

import android.content.res.Configuration
import android.os.Bundle
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.Logger
import org.teslasoft.assistant.preferences.Preferences

class LogsActivity : FragmentActivity() {

    private var btnClearLog: MaterialButton? = null

    private var btnBack: MaterialButton? = null

    private var activityLogsTitle: TextView? = null

    private var textLog: TextView? = null

    private var logType = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_logs)

        btnClearLog = findViewById(R.id.btn_clear_log)
        btnBack = findViewById(R.id.btn_back)
        activityLogsTitle = findViewById(R.id.activity_logs_title)
        textLog = findViewById(R.id.text_log)

        try {
            logType = intent.extras?.getString("type").toString()

            when (logType) {
                "crash" -> {
                    activityLogsTitle?.text = "Crash log"
                    textLog?.text = Logger.getCrashLog(this)
                }

                "ads" -> {
                    activityLogsTitle?.text = "Ads log"
                    textLog?.text = Logger.getAdsLog(this)
                }

                "event" -> {
                    activityLogsTitle?.text = "Event log"
                    textLog?.text = Logger.getEventLog(this)
                }

                else -> finish()
            }
        } catch (e: Exception) {
            finish()
        }

        btnClearLog?.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Clear log")
                .setMessage("Are you sure you want to clear this log?")
                .setPositiveButton("Yes") { _, _ ->
                    when (logType) {
                        "crash" -> {
                            Logger.clearCrashLog(this)
                            textLog?.text = Logger.getCrashLog(this)
                        }

                        "ads" -> {
                            Logger.clearAdsLog(this)
                            textLog?.text = Logger.getAdsLog(this)
                        }

                        "event" -> {
                            Logger.clearEventLog(this)
                            textLog?.text = Logger.getEventLog(this)
                        }
                    }
                }
                .setNegativeButton("No") { _, _ -> }
                .show()
        }

        btnBack?.setOnClickListener {
            finish()
        }

        reloadAmoled()
    }

    override fun onResume() {
        super.onResume()
        reloadAmoled()
    }

    private fun reloadAmoled() {
        if (isDarkThemeEnabled() &&  Preferences.getPreferences(this, "").getAmoledPitchBlack()) {
            window.navigationBarColor = ResourcesCompat.getColor(resources, R.color.amoled_window_background, theme)
            window.statusBarColor = ResourcesCompat.getColor(resources, R.color.amoled_window_background, theme)
            window.setBackgroundDrawableResource(R.color.amoled_window_background)
        } else {
            window.navigationBarColor = ResourcesCompat.getColor(resources, R.color.window_background, theme)
            window.statusBarColor = ResourcesCompat.getColor(resources, R.color.window_background, theme)
            window.setBackgroundDrawableResource(R.color.window_background)
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
}