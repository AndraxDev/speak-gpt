/**************************************************************************
 * Copyright (c) 2023-2025 Dmytro Ostapenko. All rights reserved.
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

import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.webkit.WebView
import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.material.elevation.SurfaceColors
import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.Preferences

class DocumentationActivity : FragmentActivity() {

    private var btnBack: ImageButton? = null
    private var webview: WebView? = null
    private var root: ConstraintLayout? = null
    private var preferences: Preferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_documentation)

        btnBack = findViewById(R.id.btn_back)
        webview = findViewById(R.id.webview)
        root = findViewById(R.id.root)

        Thread {
            val extras = intent.extras
            var chatId = ""

            if (extras != null) {
                chatId = extras.getString("chatId", "")
            }

            preferences = Preferences.getPreferences(this, chatId)

            runOnUiThread {
                reloadAmoled()

                btnBack?.setOnClickListener {
                    finish()
                }
            }
        }.start()
    }

    override fun onResume() {
        super.onResume()
        reloadAmoled()
    }

    private fun reloadAmoled() {
        if (isDarkThemeEnabled() && preferences?.getAmoledPitchBlack()!!) {
            if (android.os.Build.VERSION.SDK_INT <= 34) {
                window.navigationBarColor = ResourcesCompat.getColor(resources, R.color.amoled_window_background, theme)
                window.statusBarColor = ResourcesCompat.getColor(resources, R.color.amoled_window_background, theme)
            }
            window.setBackgroundDrawableResource(R.color.amoled_window_background)
            root?.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.amoled_window_background, theme))
            btnBack?.setBackgroundResource(R.drawable.btn_accent_icon_large_amoled)
        } else {
            if (android.os.Build.VERSION.SDK_INT <= 34) {
                window.navigationBarColor = SurfaceColors.SURFACE_0.getColor(this)
                window.statusBarColor = SurfaceColors.SURFACE_0.getColor(this)
            }
            window.setBackgroundDrawableResource(R.color.window_background)
            root?.setBackgroundColor(SurfaceColors.SURFACE_0.getColor(this))
            btnBack?.background = getDisabledDrawable(ResourcesCompat.getDrawable(resources, R.drawable.btn_accent_icon_large, theme)!!)
        }
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