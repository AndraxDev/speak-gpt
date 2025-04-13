package org.teslasoft.assistant.ui.activities

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.material.elevation.SurfaceColors
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
            if (android.os.Build.VERSION.SDK_INT <= 30) {
                window.navigationBarColor = ResourcesCompat.getColor(resources, R.color.amoled_window_background, theme)
                window.statusBarColor = ResourcesCompat.getColor(resources, R.color.amoled_window_background, theme)
            }

            window.setBackgroundDrawableResource(R.color.amoled_window_background)
        } else {
            if (android.os.Build.VERSION.SDK_INT <= 30) {
                window.navigationBarColor = SurfaceColors.SURFACE_0.getColor(this)
                window.statusBarColor = SurfaceColors.SURFACE_0.getColor(this)
            }
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
        WindowInsetsUtil.adjustPaddings(this, R.id.ui, EnumSet.of(WindowInsetsUtil.Companion.Flags.STATUS_BAR, WindowInsetsUtil.Companion.Flags.NAVIGATION_BAR), dpToPx(16))
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
