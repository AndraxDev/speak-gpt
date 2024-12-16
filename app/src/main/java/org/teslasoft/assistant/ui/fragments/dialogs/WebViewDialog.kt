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

package org.teslasoft.assistant.ui.fragments.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.res.Resources
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import org.teslasoft.assistant.Config
import org.teslasoft.assistant.R

class WebViewDialog : DialogFragment() {
    companion object {
        fun newInstance(url: String, title: String): WebViewDialog {
            val dialog = WebViewDialog()
            val args = Bundle()
            args.putString("url", url)
            args.putString("title", title)
            dialog.arguments = args
            return dialog
        }
    }

    private var webView: WebView? = null
    private var loadingBar: CircularProgressIndicator? = null
    private var dialogTitle: TextView? = null
    private var dialogBody: ConstraintLayout? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder =  MaterialAlertDialogBuilder(requireActivity(), R.style.App_MaterialAlertDialog)
            .setPositiveButton("Close") { _, _ -> }

        val view: View = this.layoutInflater.inflate(R.layout.dialog_webview, null)

        webView = view.findViewById(R.id.web_view)
        loadingBar = view.findViewById(R.id.loading_bar)
        dialogTitle = view.findViewById(R.id.dialog_title)
        dialogBody = view.findViewById(R.id.dialog_body)

        webView?.setBackgroundColor(0x00000000)

        val height = Resources.getSystem().displayMetrics.heightPixels

        dialogBody?.layoutParams?.height = height - 100

        dialogTitle?.text = arguments?.getString("title")

        webView?.settings?.javaScriptEnabled = true
        webView?.settings?.domStorageEnabled = true
        webView?.settings?.setSupportZoom(true)
        webView?.settings?.databaseEnabled = true
        webView?.settings?.builtInZoomControls = true
        webView?.settings?.displayZoomControls = false

        webView?.loadUrl(arguments?.getString("url") ?: Config. DEFAULT_URL)

        webView?.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                loadingBar?.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                loadingBar?.visibility = View.GONE
                dialogBody?.layoutParams?.height = height - 100
            }
        }

        builder.setView(view)

        return builder.create()

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_webview, container, false)
    }
}
