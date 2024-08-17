/*******************************************************************************
 * Copyright (c) 2023-2024 Dmytro Ostapenko. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.teslasoft.core.auth

import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class TeslasoftIDAuth : FragmentActivity() {
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                permit()
            } else {
                MaterialAlertDialogBuilder(this, R.style.TeslasoftID_MaterialAlertDialog)
                    .setTitle(R.string.teslasoft_services_auth_core_name)
                    .setMessage(R.string.teslasoft_services_auth_core_permission_denied)
                    .setCancelable(false)
                    .setPositiveButton(R.string.teslasoft_services_auth_dialog_close) { _: DialogInterface?, _: Int ->
                        this.setResult(2)
                        finish()
                    }.show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teslasoft_id)

        askAuthPermission()
    }

    private val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        try {
            if (result.resultCode >= 20) {
                val intent = Intent()
                intent.putExtra("account_id", result.data?.getStringExtra("account_id"))
                intent.putExtra("signature", result.data?.getStringExtra("signature"))
                intent.putExtra("auth_token", result.data!!.getStringExtra("auth_token"))
                this.setResult(result.resultCode, intent)
                finish()
            } else if (result.resultCode == 3 || result.resultCode == 4) {
                this.setResult(result.resultCode)
                finish()
            } else {
                MaterialAlertDialogBuilder(this, R.style.TeslasoftID_MaterialAlertDialog)
                    .setTitle(getAppName())
                    .setMessage(String.format(getString(R.string.teslasoft_services_auth_core_unavailable), getAppName()))
                    .setCancelable(false)
                    .setPositiveButton(R.string.teslasoft_services_auth_dialog_close) { _: DialogInterface?, _: Int ->
                        this.setResult(RESULT_CANCELED)
                        finish()
                    }.show()
            }
        } catch (e: Exception) {
            if (result.resultCode == 3 || result.resultCode == 4) {
                this.setResult(result.resultCode)
                finish()
            } else {
                this.setResult(result.resultCode)
                MaterialAlertDialogBuilder(this, R.style.TeslasoftID_MaterialAlertDialog)
                    .setTitle(R.string.teslasoft_services_auth_core_sync)
                    .setMessage(R.string.teslasoft_services_auth_core_required)
                    .setCancelable(false)
                    .setPositiveButton(R.string.teslasoft_services_auth_dialog_close) { _: DialogInterface?, _: Int ->
                        this.setResult(RESULT_CANCELED)
                        finish()
                    }.show()
            }
        }
    }

    private fun permit() {
        try {
            val apiIntent = Intent()
            apiIntent.component = ComponentName(
                "com.teslasoft.libraries.support",
                "org.teslasoft.core.api.account.AccountPickerActivity"
            )
            activityResultLauncher.launch(apiIntent)
        } catch (_: Exception) {
            MaterialAlertDialogBuilder(this, R.style.TeslasoftID_MaterialAlertDialog)
                .setTitle(getAppName())
                .setMessage(String.format(getString(R.string.teslasoft_services_auth_core_unavailable), getAppName()))
                .setCancelable(false)
                .setPositiveButton(R.string.teslasoft_services_auth_dialog_close) { _: DialogInterface?, _: Int ->
                    this.setResult(RESULT_CANCELED)
                    finish()
                }.show()
        }
    }

    private fun askAuthPermission() {
        if (ContextCompat.checkSelfPermission(
                this, "org.teslasoft.core.permission.AUTHENTICATE_ACCOUNTS"
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            permit()
        } else {
            MaterialAlertDialogBuilder(this, R.style.TeslasoftID_MaterialAlertDialog)
                .setTitle(R.string.teslasoft_services_auth_core_name)
                .setMessage(R.string.teslasoft_services_auth_core_permission)
                .setCancelable(false)
                .setPositiveButton(R.string.teslasoft_services_auth_permission_allow) { _: DialogInterface?, _: Int ->
                    requestPermissionLauncher.launch("org.teslasoft.core.permission.AUTHENTICATE_ACCOUNTS")
                }.setNegativeButton("No thanks") { _: DialogInterface?, _: Int ->
                    this.setResult(2)
                    finish()
                }.show()
        }
    }

    private fun Context.getAppName(): String = applicationInfo.loadLabel(packageManager).toString()
}
