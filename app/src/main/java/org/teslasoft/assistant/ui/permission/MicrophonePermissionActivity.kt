/**************************************************************************
 * Copyright (c) 2023 Dmytro Ostapenko. All rights reserved.
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

package org.teslasoft.assistant.ui.permission

import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle

import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

import com.google.android.material.dialog.MaterialAlertDialogBuilder

import org.teslasoft.assistant.R

class MicrophonePermissionActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        askNotificationPermission()
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                this.setResult(RESULT_OK)
                finish()
            } else {
                MaterialAlertDialogBuilder(this, R.style.App_MaterialAlertDialog)
                    .setTitle("SpeakGPT")
                    .setMessage("You can not use this feature because app do not have microphone access.")
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                        this.setResult(RESULT_CANCELED)
                        finish()
                    }.show()
            }
        }

    private fun askNotificationPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            setResult(RESULT_OK)
            finish()
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
            MaterialAlertDialogBuilder(this, R.style.App_MaterialAlertDialog)
                .setTitle("SpeakGPT")
                .setMessage("SpeakGPT allows you to interact with ChatGPT via voice activation. To enable voice activation please allow this app to use your device microphone.")
                .setCancelable(false)
                .setPositiveButton("Allow") { _: DialogInterface?, _: Int ->
                    requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }.setNegativeButton("No thanks") { _: DialogInterface?, _: Int ->
                    this.setResult(RESULT_CANCELED)
                    finish()
                }.show()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}
