package org.teslasoft.assistant.onboarding

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Process
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.teslasoft.assistant.ChatActivity
import org.teslasoft.assistant.ChatsListActivity
import org.teslasoft.assistant.R

class ActivationActivity : FragmentActivity() {

    private var btnNext: MaterialButton? = null
    private var webView: WebView? = null
    private var keyInput: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activation)

        btnNext = findViewById(R.id.btn_next)
        webView = findViewById(R.id.webview)
        keyInput = findViewById(R.id.key_input)

        webView?.setBackgroundColor(0x00000000)

        val processName = getProcessName(this)
        try {
            WebView.setDataDirectorySuffix(processName!!)
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
                    MaterialAlertDialogBuilder(this@ActivationActivity, R.style.App_MaterialAlertDialog)
                        .setTitle("Error opening link")
                        .setMessage("You need a web browser installed to perform this action")
                        .setPositiveButton("Close") { _, _ -> }
                        .show()
                }
                return true
            }
        }

        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> webView?.loadUrl("file:///android_asset/www/api.html")
            Configuration.UI_MODE_NIGHT_NO -> webView?.loadUrl("file:///android_asset/www/api_light.html")
        }

        btnNext?.setOnClickListener {
            if (keyInput?.text.toString() == "") {
                Toast.makeText(this, "Please enter an API key", Toast.LENGTH_SHORT).show()
            } else {
                val settings: SharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)
                val editor: SharedPreferences.Editor = settings.edit()
                editor.putString("api_key", keyInput?.text.toString())
                editor.apply()
                startActivity(Intent(this, ChatsListActivity::class.java))
            }
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