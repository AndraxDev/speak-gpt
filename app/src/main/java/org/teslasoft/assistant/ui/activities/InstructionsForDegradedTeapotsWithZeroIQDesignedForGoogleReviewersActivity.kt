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

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.Preferences
import org.teslasoft.assistant.util.WindowInsetsUtil
import java.util.EnumSet

class InstructionsForDegradedTeapotsWithZeroIQDesignedForGoogleReviewersActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.instructions_for_degraded_teapots_with_zero_iq_designed_for_google_reviewers)
        reloadAmoled()
    }

    private fun reloadAmoled() {
        if (isDarkThemeEnabled() && Preferences.getPreferences(this, "").getAmoledPitchBlack()) {
            window.setBackgroundDrawableResource(R.color.amoled_window_background)
        } else {
            window.setBackgroundDrawableResource(R.color.window_background)
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

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        WindowInsetsUtil.adjustPaddings(this, R.id.ui, EnumSet.of(WindowInsetsUtil.Companion.Flags.STATUS_BAR, WindowInsetsUtil.Companion.Flags.NAVIGATION_BAR), (16 * resources.displayMetrics.density).toInt())
    }
}
