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

package org.teslasoft.assistant.pwa

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.webkit.WebView
import android.widget.ImageButton
import android.widget.ProgressBar
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.teslasoft.assistant.Config.Companion.API_SERVER_NAME
import org.teslasoft.assistant.R

class PWAActivity : FragmentActivity() {
    private var webView: WebView? = null
    private var loader: ProgressBar? = null
    private var btnClose: ImageButton? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.navigationBarColor = ResourcesCompat.getColor(resources, R.color.pwa_background, null)
        window.statusBarColor = ResourcesCompat.getColor(resources, R.color.pwa_background, null)

        setContentView(R.layout.activity_pwa)

        webView = findViewById(R.id.web_view)
        loader = findViewById(R.id.loader)
        btnClose = findViewById(R.id.btn_close)

        btnClose?.setOnClickListener {
            closePWA()
        }

        webView?.settings?.javaScriptEnabled = true
        webView?.settings?.domStorageEnabled = true
        webView?.settings?.databaseEnabled = true
        webView?.settings?.cacheMode = android.webkit.WebSettings.LOAD_CACHE_ELSE_NETWORK
        webView?.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.pwa_background, null))

        loader?.visibility = ProgressBar.GONE

        webView?.webViewClient = object : android.webkit.WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                loader?.visibility = ProgressBar.GONE
            }
        }

        webView?.loadUrl("https://$API_SERVER_NAME/chat")

        if (Build.VERSION.SDK_INT >= 33) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) {
                webView?.let {
                    if (it.canGoBack()) {
                        it.goBack()
                        return@registerOnBackInvokedCallback
                    } else {
                        closePWA()
                    }
                }
            }
        } else {
            onBackPressedDispatcher.addCallback(this /* lifecycle owner */, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    webView?.let {
                        if (it.canGoBack()) {
                            it.goBack()
                            return
                        } else {
                            closePWA()
                        }
                    }
                }
            })
        }
    }

    private fun closePWA() {
        MaterialAlertDialogBuilder(this)
            .setTitle("PWA")
            .setMessage("Are you sure you want to close the PWA?")
            .setPositiveButton("Exit") { _, _ ->
                finish()
            }
            .setNeutralButton("Clear cache and reload") { _, _ ->
                webView?.clearCache(true)
                webView?.reload()
            }
            .setNegativeButton("Cancel") { _, _ -> }
            .show()
    }
}