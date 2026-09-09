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

package org.teslasoft.assistant.ui.liquidglass

import android.app.Activity
import com.example.liquidglass.LiquidGlassView

class LiquidGlassUtil {
    companion object {
        fun initializeLiquidGlassViewById(viewId: Int, activity: Activity) {
            val liquidGlassView = activity.findViewById<LiquidGlassView>(viewId)
            liquidGlassView.enableDynamicBackground = true
        }

        fun setAccentColorForLiquidGlassView(viewId: Int, activity: Activity) {
            val liquidGlassView = activity.findViewById<LiquidGlassView>(viewId)
            if (android.os.Build.VERSION.SDK_INT >= 31) {
                val systemAccentDark = activity.getColor(android.R.color.system_accent1_600)
                val systemAccentLight = activity.getColor(android.R.color.system_accent1_100)
                liquidGlassView.glassTint = if (isDarkModeEnabled(activity)) systemAccentDark else systemAccentLight
            }
        }

        fun isDarkModeEnabled(activity: Activity): Boolean {
            val uiMode = activity.resources.configuration.uiMode
            val nightModeFlags = uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
            return nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES
        }
    }
}
