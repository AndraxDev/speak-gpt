package org.teslasoft.assistant.pwa

import android.os.Build
import android.os.Bundle
import android.webkit.WebView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.navigationBarColor = ResourcesCompat.getColor(resources, R.color.pwa_background, null)
        window.statusBarColor = ResourcesCompat.getColor(resources, R.color.pwa_background, null)

        setContentView(R.layout.activity_pwa)

        webView = findViewById(R.id.webView)
        loader = findViewById(R.id.loader)

        webView?.settings?.javaScriptEnabled = true
        webView?.settings?.domStorageEnabled = true
        webView?.settings?.databaseEnabled = true
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
                        finish()
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
                            finish()
                        }
                    }
                }
            })
        }
    }
}