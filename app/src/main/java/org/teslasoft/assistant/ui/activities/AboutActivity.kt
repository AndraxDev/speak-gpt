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

package org.teslasoft.assistant.ui.activities

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.elevation.SurfaceColors
import org.teslasoft.assistant.Config
import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.Preferences
import org.teslasoft.core.auth.SystemInfo

class AboutActivity : FragmentActivity() {

    private var appIcon: ImageView? = null
    private var btnProjects: MaterialButton? = null
    private var btnTerms: MaterialButton? = null
    private var btnPrivacy: MaterialButton? = null
    private var btnFeedback: MaterialButton? = null
    private var appVer: TextView? = null
    private var tidVer: TextView? = null
    private var btnDonate: LinearLayout? = null
    private var btnGithub: LinearLayout? = null

    private var actions: ConstraintLayout? = null
    private var system: ConstraintLayout? = null
    private var links: LinearLayout? = null

    private var root: ConstraintLayout? = null

    private var btnBack: ImageButton? = null

    private var activateEasterEggCounter: Int = 0

    private var preferences: Preferences? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_about)

        appIcon = findViewById(R.id.app_icon)
        btnProjects = findViewById(R.id.btn_projects)
        btnTerms = findViewById(R.id.btn_terms)
        btnPrivacy = findViewById(R.id.btn_privacy)
        btnFeedback = findViewById(R.id.btn_feedback)
        appVer = findViewById(R.id.app_ver)
        tidVer = findViewById(R.id.tid_ver)
        btnDonate = findViewById(R.id.btn_donate)
        btnGithub = findViewById(R.id.btn_github)
        btnBack = findViewById(R.id.btn_back)

        actions = findViewById(R.id.common_actions)
        system = findViewById(R.id.system_info)
        links = findViewById(R.id.links)

        root = findViewById(R.id.root)

        appIcon?.setImageResource(R.drawable.assistant)

        val extras = intent.extras
        var chatId = ""

        if (extras != null) {
            chatId = extras.getString("chatId", "")
        }

        preferences = Preferences.getPreferences(this, chatId)

        reloadAmoled()

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
                } catch (e: Exception) {
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
        } catch (e: PackageManager.NameNotFoundException) {
            appVer?.text = "${getString(R.string.app_version)} unknown"
        }

        btnProjects?.setOnClickListener {
            val i = Intent()
            i.data = Uri.parse("https://andrax.dev/")
            i.action = Intent.ACTION_VIEW
            startActivity(i)
        }

        btnTerms?.setOnClickListener {
            val i = Intent()
            i.data = Uri.parse("https://${Config.API_SERVER_NAME}/terms")
            i.action = Intent.ACTION_VIEW
            startActivity(i)
        }

        btnPrivacy?.setOnClickListener {
            val i = Intent()
            i.data = Uri.parse("https://${Config.API_SERVER_NAME}/privacy")
            i.action = Intent.ACTION_VIEW
            startActivity(i)
        }

        btnFeedback?.setOnClickListener {
            val i = Intent()
            i.data = Uri.parse("mailto:dostapenko82@gmail.com")
            i.action = Intent.ACTION_VIEW
            startActivity(i)
        }

        btnDonate?.setOnClickListener {
            val i = Intent()
            i.data = Uri.parse("https://buymeacoffee.com/andrax_dev")
            i.action = Intent.ACTION_VIEW
            startActivity(i)
        }

        btnGithub?.setOnClickListener {
            val i = Intent()
            i.data = Uri.parse("https://github.com/AndraxDev/speak-gpt")
            i.action = Intent.ACTION_VIEW
            startActivity(i)
        }

        btnBack?.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        reloadAmoled()
    }

    private fun reloadAmoled() {
        if (isDarkThemeEnabled() && Preferences.getPreferences(this, "").getAmoledPitchBlack()) {
            if (android.os.Build.VERSION.SDK_INT <= 34) {
                window.navigationBarColor = ResourcesCompat.getColor(resources, R.color.amoled_window_background, theme)
                window.statusBarColor = ResourcesCompat.getColor(resources, R.color.amoled_window_background, theme)
            }

            window.setBackgroundDrawableResource(R.color.amoled_window_background)

            appIcon?.setBackgroundResource(R.drawable.btn_accent_50_amoled)

            actions?.setBackgroundResource(R.drawable.btn_accent_24_amoled)
            system?.setBackgroundResource(R.drawable.btn_accent_24_amoled)
            links?.setBackgroundResource(R.drawable.btn_accent_24_amoled)

            root?.setBackgroundResource(R.color.amoled_window_background)
            btnBack?.setBackgroundResource(R.drawable.btn_accent_icon_large_amoled)
        } else {
            if (android.os.Build.VERSION.SDK_INT <= 34) {
                window.navigationBarColor = SurfaceColors.SURFACE_0.getColor(this)
                window.statusBarColor = SurfaceColors.SURFACE_0.getColor(this)
            }
            window.setBackgroundDrawableResource(R.color.window_background)

            appIcon?.setBackgroundResource(R.drawable.btn_accent_50)

            actions?.background = getDarkDrawable(ResourcesCompat.getDrawable(resources, R.drawable.btn_accent_24, theme)!!)
            system?.background = getDarkDrawable(ResourcesCompat.getDrawable(resources, R.drawable.btn_accent_24, theme)!!)
            links?.background = getDarkDrawable(ResourcesCompat.getDrawable(resources, R.drawable.btn_accent_24, theme)!!)

            root?.setBackgroundColor(SurfaceColors.SURFACE_0.getColor(this))
            btnBack?.background = getDisabledDrawable(ResourcesCompat.getDrawable(resources, R.drawable.btn_accent_icon_large, theme)!!)
        }
    }

    private fun getDarkDrawable(drawable: Drawable) : Drawable {
        DrawableCompat.setTint(DrawableCompat.wrap(drawable), SurfaceColors.SURFACE_2.getColor(this))
        return drawable
    }

    private fun isDarkThemeEnabled(): Boolean {
        return when (resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            Configuration.UI_MODE_NIGHT_NO -> false
            Configuration.UI_MODE_NIGHT_UNDEFINED -> false
            else -> false
        }
    }

    private fun getDisabledDrawable(drawable: Drawable) : Drawable {
        DrawableCompat.setTint(DrawableCompat.wrap(drawable), getDisabledColor())
        return drawable
    }

    private fun getDisabledColor() : Int {
        return if (isDarkThemeEnabled() && preferences?.getAmoledPitchBlack() == true) {
            ResourcesCompat.getColor(resources, R.color.accent_50, theme)
        } else {
            SurfaceColors.SURFACE_5.getColor(this)
        }
    }
}
