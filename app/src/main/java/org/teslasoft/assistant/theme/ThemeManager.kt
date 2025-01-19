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

package org.teslasoft.assistant.theme

import android.content.Context
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.elevation.SurfaceColors
import org.teslasoft.assistant.R

class ThemeManager {
    companion object {
        private var themeManager: ThemeManager? = null

        @JvmStatic
        fun getThemeManager(): ThemeManager {
            if (themeManager == null) themeManager = ThemeManager()
            return themeManager!!
        }
    }

    fun applyTheme(context: Context, isAmoled: Boolean) {
        // Purple magic
        recolor(R.drawable.btn_accent_tonal_v4, context, isAmoled)
        recolor(R.drawable.btn_accent_tonal_v5, context, isAmoled)
        recolor(R.drawable.btn_accent_icon_large_100, context, isAmoled)
    }

    private fun recolor(color: Int, context: Context, isAmoled: Boolean) {
        if (isAmoled) {
            DrawableCompat.setTint(
                DrawableCompat.wrap(
                    AppCompatResources.getDrawable(
                        context, color
                    )!!
                ), getAmoledSurfaceColor(context)
            )
        } else {
            DrawableCompat.setTint(
                DrawableCompat.wrap(
                    AppCompatResources.getDrawable(
                        context, color
                    )!!
                ), getSurfaceColor(context)
            )
        }
    }

    private fun getAmoledSurfaceColor(context: Context): Int {
        return ContextCompat.getColor(context, R.color.amoled_accent_50)
    }

    private fun getSurfaceColor(context: Context): Int {
        return SurfaceColors.SURFACE_4.getColor(context)
    }
}
