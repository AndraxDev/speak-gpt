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

import androidx.fragment.app.FragmentActivity

import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

import org.teslasoft.assistant.R

class TermsActivity : FragmentActivity() {

    private var btnNext: MaterialButton? = null
    private var webView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms)

        btnNext = findViewById(R.id.btn_next)
        webView = findViewById(R.id.webview)

        webView?.setBackgroundColor(0x00000000)

        val processName = getProcessName(this)
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                WebView.setDataDirectorySuffix(processName!!)
            }
        } catch (ignored: Exception) { /* unused */
        }

        webView?.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) { /* unused */
            }

            override fun shouldOverrideUrlLoading(
                view: WebView, request: WebResourceRequest
            ): Boolean {
                try {
                    val intent = Intent()
                    intent.action = Intent.ACTION_VIEW
                    intent.data = Uri.parse(request.url.toString())
                    startActivity(intent)
                } catch (e: Exception) {
                    MaterialAlertDialogBuilder(this@TermsActivity, R.style.App_MaterialAlertDialog)
                        .setTitle("Error opening link")
                        .setMessage("You need a web browser installed to perform this action")
                        .setPositiveButton("Close") { _, _ -> }
                        .show()
                }
                return true
            }
        }

        /*when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> webView?.loadUrl("file:///android_asset/www/privacy.html")
            Configuration.UI_MODE_NIGHT_NO -> webView?.loadUrl("file:///android_asset/www/privacy_light.html")
        }*/

        btnNext?.setOnClickListener {
            startActivity(Intent(this, ActivationActivity::class.java).setAction(Intent.ACTION_VIEW))
            finish()
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
