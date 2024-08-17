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

package org.teslasoft.core

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentActivity
import cat.ereza.customactivityoncrash.CustomActivityOnCrash
import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.DeviceInfoProvider
import org.teslasoft.assistant.preferences.Logger
import org.teslasoft.assistant.ui.activities.MainActivity
import org.teslasoft.core.auth.SystemInfo
import java.time.Instant
import java.time.format.DateTimeFormatter

/** This activity will be opened if app os crashed. */
@Suppress("DEPRECATION")
class CrashHandlerActivity : FragmentActivity() {

    private var error: String? = null
    private var textError: TextView? = null

    @SuppressLint("SetTextI18n", "HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT <= 34) {
            window.statusBarColor = getColor(R.color.amoled_window_background)
            window.navigationBarColor = getColor(R.color.amoled_window_background)
        }

        val appVersion = try {
            val pInfo: PackageInfo = if (Build.VERSION.SDK_INT >= 33) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                packageManager.getPackageInfo(packageName, 0)
            }

            val version = pInfo.versionName

            version
        } catch (e: PackageManager.NameNotFoundException) {
            "unknown"
        }

        val versionCode = try {
            val pInfo: PackageInfo = if (Build.VERSION.SDK_INT >= 33) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                packageManager.getPackageInfo(packageName, 0)
            }

            val version = pInfo.longVersionCode

            version
        } catch (e: PackageManager.NameNotFoundException) {
            "unknown"
        }

        try {
            error = CustomActivityOnCrash.getStackTraceFromIntent(intent)

            setContentView(R.layout.activity_crash)

            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finishAndRemoveTask()
                }
            })

            val IID = when (val installationId = DeviceInfoProvider.getInstallationId(this)) {
                "00000000-0000-0000-0000-000000000000" -> "<Authorization revoked>"
                "" -> "<Not assigned>"
                else -> installationId
            }

            textError = findViewById(R.id.text_error)
            textError!!.setTextIsSelectable(true)
            textError!!.text = "\nApp has been crashed and needs to be restarted.\n\n===== BEGIN SYSTEM INFO =====\nAndroid version: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT} ${Build.VERSION.CODENAME})\nROM version: ${Build.VERSION.INCREMENTAL}\nApp version: $appVersion ($versionCode)\nDevice model: ${Build.MODEL}\nAndroid device ID: ${Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)}\nInstallation ID: ${IID}\nTeslasoft ID version: ${SystemInfo.VERSION} (${SystemInfo.VERSION_CODE})\nEffective time: ${
                DateTimeFormatter.ISO_INSTANT.format(
                    Instant.now())}\n===== END SYSTEM INFO =====\n\n===== BEGIN OF CRASH =====\n$error\n===== END OF CRASH =====\n"

            Logger.clearCrashLog(this)
            Logger.log(this, "crash", "CrashHandler", "error", textError!!.text.toString())
            if (error == "") {
                finishAndRemoveTask()
            }
        } catch (_: Exception) {
            finishAndRemoveTask()
        }
    }

    fun restart(v: View?) {
        startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        finish()
    }

    fun copy(v: View?) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Error", textError!!.text.toString())
        clipboard.setPrimaryClip(clip)

        Toast.makeText(
            applicationContext,
            R.string.label_copy,
            Toast.LENGTH_SHORT
        ).show()
    }
}
