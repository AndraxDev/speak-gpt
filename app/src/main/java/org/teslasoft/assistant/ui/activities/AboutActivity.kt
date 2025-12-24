/**************************************************************************
 * Copyright (c) 2023-2026 Dmytro Ostapenko. All rights reserved.
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

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import com.google.android.material.button.MaterialButton
import org.teslasoft.assistant.Config
import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.Preferences
import org.teslasoft.assistant.util.WindowInsetsUtil
import org.teslasoft.core.auth.SystemInfo
import java.util.EnumSet
import androidx.core.net.toUri
import eightbitlab.com.blurview.BlurView

class AboutActivity : FragmentActivity() {

    private var appIcon: ImageView? = null
    private var btnProjects: MaterialButton? = null
    private var btnTerms: MaterialButton? = null
    private var btnPrivacy: MaterialButton? = null
    private var btnFeedback: MaterialButton? = null
    private var appVer: TextView? = null
    private var tidVer: TextView? = null
    private var btnDonate: MaterialButton? = null
    private var btnGithub: MaterialButton? = null

    private var btnBack: ImageButton? = null

    private var activateEasterEggCounter: Int = 0

    private var preferences: Preferences? = null

    private var foregroundBlur: BlurView? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_about_new)

        appIcon = findViewById(R.id.app_icon)
        btnProjects = findViewById(R.id.btn_projects)
        btnTerms = findViewById(R.id.btn_terms)
        btnPrivacy = findViewById(R.id.btn_privacy)
        btnFeedback = findViewById(R.id.btn_feedback)
        appVer = findViewById(R.id.app_ver)
        btnDonate = findViewById(R.id.btn_donate)
        btnGithub = findViewById(R.id.btn_github)
        btnBack = findViewById(R.id.btn_back)
        foregroundBlur = findViewById(R.id.foreground_blur)

        appIcon?.setImageResource(R.drawable.assistant)

        // Deprecated renderscript seems does not work properly on the older android versions
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S) {
            val tl = findViewById<ConstraintLayout>(R.id.tl)
            val tr = findViewById<ConstraintLayout>(R.id.tr)
            tl?.visibility = ConstraintLayout.GONE
            tr?.visibility = ConstraintLayout.GONE
        } else {
            val decorView = window.decorView
            val rootView: ViewGroup = decorView.findViewById(android.R.id.content)
            val windowBackground = decorView.background

            foregroundBlur?.setupWith(rootView)?.setFrameClearDrawable(windowBackground)?.setBlurRadius(250f)
        }

        val extras = intent.extras
        var chatId = ""

        if (extras != null) {
            chatId = extras.getString("chatId", "")
        }

        preferences = Preferences.getPreferences(this, chatId)

        appVer?.setOnClickListener {
            if (activateEasterEggCounter == 0) {
                Handler(Looper.getMainLooper()).postDelayed({
                    activateEasterEggCounter = 0
                }, 1500)
            }

            if (activateEasterEggCounter == 4) {
                activateEasterEggCounter = 0

                try {
                    val intent = Intent(Intent.ACTION_MAIN)
                    intent.setComponent(ComponentName("com.teslasoft.libraries.support", "org.teslasoft.core.easter.JarvisPlatLogo"))
                    startActivity(intent)
                } catch (_: Exception) {
                    /* TODO: Open easter egg */
                    Toast.makeText(this, "Easter egg found!", Toast.LENGTH_SHORT).show()
                }
            }

            activateEasterEggCounter++
        }

        tidVer?.text = "${getString(R.string.teslasoft_id_version)} ${SystemInfo.VERSION} (${SystemInfo.VERSION_CODE})"

        try {
            val pInfo: PackageInfo = if (android.os.Build.VERSION.SDK_INT >= 33) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                packageManager.getPackageInfo(packageName, 0)
            }

            val version = pInfo.versionName

            appVer?.text = "${getString(R.string.app_version)} $version"
        } catch (_: PackageManager.NameNotFoundException) {
            appVer?.text = "${getString(R.string.app_version)} unknown"
        }

        btnProjects?.setOnClickListener {
            val i = Intent()
            i.data = "https://andrax.dev/".toUri()
            i.action = Intent.ACTION_VIEW
            startActivity(i)
        }

        btnTerms?.setOnClickListener {
            val i = Intent()
            i.data = "https://${Config.API_SERVER_NAME}/terms".toUri()
            i.action = Intent.ACTION_VIEW
            startActivity(i)
        }

        btnPrivacy?.setOnClickListener {
            val i = Intent()
            i.data = "https://${Config.API_SERVER_NAME}/privacy".toUri()
            i.action = Intent.ACTION_VIEW
            startActivity(i)
        }

        btnFeedback?.setOnClickListener {
            val i = Intent()
            i.data = "mailto:dostapenko82@gmail.com".toUri()
            i.action = Intent.ACTION_VIEW
            startActivity(i)
        }

        btnDonate?.setOnClickListener {
            val i = Intent()
            i.data = "https://ko-fi.com/andrax_dev".toUri()
            i.action = Intent.ACTION_VIEW
            startActivity(i)
        }

        btnGithub?.setOnClickListener {
            val i = Intent()
            i.data = "https://github.com/AndraxDev/speak-gpt".toUri()
            i.action = Intent.ACTION_VIEW
            startActivity(i)
        }

        btnBack?.setOnClickListener {
            finish()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        WindowInsetsUtil.adjustPaddings(this, R.id.scrollable, EnumSet.of(WindowInsetsUtil.Companion.Flags.STATUS_BAR, WindowInsetsUtil.Companion.Flags.NAVIGATION_BAR))
    }
}
