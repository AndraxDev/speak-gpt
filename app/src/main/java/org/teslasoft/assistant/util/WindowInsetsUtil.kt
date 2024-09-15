package org.teslasoft.assistant.util

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.View
import android.view.WindowInsets
import java.util.EnumSet

class WindowInsetsUtil {
    companion object {
        enum class Flags {
            STATUS_BAR,
            NAVIGATION_BAR,
            IGNORE_PADDINGS
        }

        fun adjustPaddings(activity: Activity, res: Int, flags: EnumSet<Flags>, customPaddingTop: Int = 0, customPaddingBottom: Int = 0) {
            if (Build.VERSION.SDK_INT < 30) return
            try {
                val view = activity.findViewById<View>(res)
                view.setPadding(
                    0,
                    activity.window.decorView.rootWindowInsets.getInsets(WindowInsets.Type.statusBars()).top * (if (flags.contains(Flags.STATUS_BAR)) 1 else 0) + view.paddingTop * (if (flags.contains(Flags.IGNORE_PADDINGS)) 0 else 1) + pxToDp(activity, customPaddingTop),
                    0,
                    activity.window.decorView.rootWindowInsets.getInsets(WindowInsets.Type.navigationBars()).bottom * (if (flags.contains(Flags.NAVIGATION_BAR)) 1 else 0) + view.paddingBottom * (if (flags.contains(Flags.IGNORE_PADDINGS)) 0 else 1) + pxToDp(activity, customPaddingBottom)
                )
            } catch (_: Exception) { /* unused */ }
        }

        private fun pxToDp(context: Context, px: Int): Int {
            return (px / context.resources.displayMetrics.density).toInt()
        }
    }
}