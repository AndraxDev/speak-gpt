/*******************************************************************************
 * Copyright (c) 2023-2025 Dmytro Ostapenko. All rights reserved.
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
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.transition.TransitionInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.util.Pair
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentActivity
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.teslasoft.core.auth.internal.ApplicationSignature

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
                        finishActivity()
                    }.show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_teslasoft_id)

        val transition = TransitionInflater.from(this).inflateTransition(android.R.transition.move).apply {
            interpolator = LinearOutSlowInInterpolator()
            duration = 375
        }

        val transition2 = TransitionInflater.from(this).inflateTransition(android.R.transition.move).apply {
            interpolator = FastOutLinearInInterpolator()
            duration = 200
        }

        // Set the transition as the shared element enter transition
        window.sharedElementEnterTransition = transition
        window.sharedElementExitTransition = transition2

        if (Build.VERSION.SDK_INT >= 33) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) {
                /* Lock back gesture */
            }
        } else {
            onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    /* Lock back gesture */
                }
            })
        }

        val signature = ApplicationSignature(this).getCertificateFingerprint("SHA256")
        val requestNetwork = RequestNetwork(this)
        requestNetwork.startRequestNetwork("GET", "https://id.teslasoft.org/xauth/CheckAppIntegrity?i=$packageName&s=$signature", "A", object : RequestNetwork.RequestListener {
            override fun onResponse(tag: String, message: String) {
                if (message == "OK") {
                    runOnUiThread {
                        if (!checkInstallation()) {
                            MaterialAlertDialogBuilder(this@TeslasoftIDAuth, R.style.TeslasoftID_MaterialAlertDialog)
                                .setTitle(getAppName())
                                .setMessage(String.format(getString(R.string.teslasoft_services_auth_core_unavailable), getAppName()))
                                .setCancelable(false)
                                .setPositiveButton(R.string.teslasoft_services_auth_dialog_close) { _: DialogInterface?, _: Int ->
                                    this@TeslasoftIDAuth.setResult(RESULT_CANCELED)
                                    finishActivity()
                                }.show()
                        } else {
                            askAuthPermission()
                        }
                    }
                } else {
                    runOnUiThread {
                        MaterialAlertDialogBuilder(this@TeslasoftIDAuth, R.style.TeslasoftID_MaterialAlertDialog)
                            .setTitle(getAppName())
                            .setMessage("Failed to verify app integrity.\n\nError detail: $message")
                            .setCancelable(false)
                            .setPositiveButton(R.string.teslasoft_services_auth_dialog_close) { _: DialogInterface?, _: Int ->
                                this@TeslasoftIDAuth.setResult(RESULT_CANCELED)
                                finishActivity()
                            }.show()
                    }
                }
            }

            override fun onErrorResponse(tag: String, message: String) {
                runOnUiThread {
                    MaterialAlertDialogBuilder(this@TeslasoftIDAuth, R.style.TeslasoftID_MaterialAlertDialog)
                        .setTitle(getAppName())
                        .setMessage("Failed to verify app integrity.\n\nError detail: $message")
                        .setCancelable(false)
                        .setPositiveButton(R.string.teslasoft_services_auth_dialog_close) { _: DialogInterface?, _: Int ->
                            this@TeslasoftIDAuth.setResult(RESULT_CANCELED)
                            finishActivity()
                        }.show()
                }
            }
        })
    }

    private fun checkInstallation(): Boolean {
        return try {
            val packageManager = this.packageManager
            val packageInfo = packageManager.getPackageInfo("com.teslasoft.libraries.support", PackageManager.GET_ACTIVITIES)

            return packageInfo != null
        } catch (_: PackageManager.NameNotFoundException) { false }
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
                Handler(mainLooper).postDelayed({
                    finishActivity()
                }, 300)
            } else if (result.resultCode == 3 || result.resultCode == 4) {
                this.setResult(result.resultCode)
                Handler(mainLooper).postDelayed({
                    finishActivity()
                }, 300)
            } else {
                MaterialAlertDialogBuilder(this, R.style.TeslasoftID_MaterialAlertDialog)
                    .setTitle(getAppName())
                    .setMessage(String.format(getString(R.string.teslasoft_services_auth_core_unavailable), getAppName()))
                    .setCancelable(false)
                    .setPositiveButton(R.string.teslasoft_services_auth_dialog_close) { _: DialogInterface?, _: Int ->
                        this.setResult(RESULT_CANCELED)
                        Handler(mainLooper).postDelayed({
                            finishActivity()
                        }, 300)
                    }.show()
            }
        } catch (e: Exception) {
            if (result.resultCode == 3 || result.resultCode == 4) {
                this.setResult(result.resultCode)
                Handler(mainLooper).postDelayed({
                    finishActivity()
                }, 300)
            } else {
                this.setResult(result.resultCode)
                MaterialAlertDialogBuilder(this, R.style.TeslasoftID_MaterialAlertDialog)
                    .setTitle(R.string.teslasoft_services_auth_core_sync)
                    .setMessage(R.string.teslasoft_services_auth_core_required)
                    .setCancelable(false)
                    .setPositiveButton(R.string.teslasoft_services_auth_dialog_close) { _: DialogInterface?, _: Int ->
                        this.setResult(RESULT_CANCELED)
                        Handler(mainLooper).postDelayed({
                            finishActivity()
                        }, 300)
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
            val sharedElement: View = findViewById(R.id.root)
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                Pair.create(sharedElement, ViewCompat.getTransitionName(sharedElement))
            )

            Handler(mainLooper).postDelayed({
                activityResultLauncher.launch(apiIntent, options)
            }, 600)
        } catch (_: Exception) {
            MaterialAlertDialogBuilder(this, R.style.TeslasoftID_MaterialAlertDialog)
                .setTitle(getAppName())
                .setMessage(String.format(getString(R.string.teslasoft_services_auth_core_unavailable), getAppName()))
                .setCancelable(false)
                .setPositiveButton(R.string.teslasoft_services_auth_dialog_close) { _: DialogInterface?, _: Int ->
                    this.setResult(RESULT_CANCELED)
                    Handler(mainLooper).postDelayed({
                        finishActivity()
                    }, 300)
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
                    Handler(mainLooper).postDelayed({
                        finishActivity()
                    }, 300)
                }.show()
        }
    }

    private fun Context.getAppName(): String = applicationInfo.loadLabel(packageManager).toString()

    fun finishActivity() {
        val expandableElement1: View? = findViewById(R.id.expandable_element_1)
        val expandableElement2: View? = findViewById(R.id.expandable_element_2)

        if (expandableElement1 == null || expandableElement2 == null) {
            supportFinishAfterTransition()
        } else {
            expandableElement1.animate().alpha(0.0f).interpolator = AnimationUtils.loadInterpolator(this, android.R.interpolator.fast_out_linear_in)
            expandableElement2.animate().alpha(0.0f).interpolator = AnimationUtils.loadInterpolator(this, android.R.interpolator.fast_out_linear_in)
            supportFinishAfterTransition()
        }
    }
}
