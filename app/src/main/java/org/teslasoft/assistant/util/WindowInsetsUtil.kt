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
