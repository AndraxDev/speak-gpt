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

package org.teslasoft.assistant.ui.assistant

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.StrictMode
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.google.android.material.elevation.SurfaceColors
import org.teslasoft.assistant.preferences.Preferences
import org.teslasoft.assistant.ui.fragments.AssistantFragment

class AssistantActivity : FragmentActivity() {

    private var restoreFromState = false

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        if (android.os.Build.VERSION.SDK_INT >= 34) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0, Color.TRANSPARENT)
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0, Color.TRANSPARENT)
        } else {
            overridePendingTransition(0, 0)
        }

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        window.navigationBarColor = SurfaceColors.SURFACE_1.getColor(this)

        if (savedInstanceState == null) {
            Handler(mainLooper).postDelayed({
                if (!restoreFromState) {
                    showAssistant()
                }
            }, 150)
        }
    }

    private fun showAssistant() {
        val assistantFragment = AssistantFragment()
        assistantFragment.isCancelable = !Preferences.getPreferences(this, "").getLockAssistantWindow()
        assistantFragment.show(supportFragmentManager, "AssistantFragment")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        restoreFromState = true
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        restoreFromState = false
    }
}
