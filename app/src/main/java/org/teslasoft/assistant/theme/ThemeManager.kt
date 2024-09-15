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