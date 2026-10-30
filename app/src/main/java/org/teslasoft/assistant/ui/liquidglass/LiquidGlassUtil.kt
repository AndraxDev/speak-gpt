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
import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.example.liquidglass.GlassAccessibilityMode
import com.example.liquidglass.GlassMaterial
import com.example.liquidglass.LiquidGlassView
import org.teslasoft.assistant.R
import org.teslasoft.assistant.ui.activities.nav.LiquidGlassTabBar

@Suppress("unused")
class LiquidGlassUtil {
    companion object {
        // For fragments
        fun initializeLiquidGlassViewById(viewId: Int, parentView: View) {
            val liquidGlassView = parentView.findViewById<org.teslasoft.assistant.ui.liquidglass.LiquidGlassView>(viewId)
            val context = parentView.context

            liquidGlassView.enableDynamicBackground = true
            liquidGlassView.edgeHighlightOpacity = 60.0f
            liquidGlassView.edgeHighlightBorderWidth = dpF(2, context)
            liquidGlassView.blurAmount = 0.5f
            liquidGlassView.pressScale = 0.9f
            liquidGlassView.enableSensorHighlight = true
            liquidGlassView.enableAdaptiveTint = true
            liquidGlassView.material = GlassMaterial.CLEAR
            liquidGlassView.refractionHeight = dpF(128, context)
            liquidGlassView.bevelWidth = dpF(32, context)
            liquidGlassView.refractionFalloff = 0.1f
            liquidGlassView.dispersionStrength = 0.2f

            if (liquidGlassView !is LiquidGlassTabBar) {
                liquidGlassView.cornerRadius = dpF(999, context)
            }

            if (android.os.Build.VERSION.SDK_INT < 33) {
                liquidGlassView.accessibilityMode = GlassAccessibilityMode.FORCE_OPAQUE
            }
        }

        // For activities
        fun initializeLiquidGlassViewById(viewId: Int, activity: Activity) {
            initializeLiquidGlassViewById(viewId, activity.findViewById(android.R.id.content))
        }

        // For fragments
        fun setAccentColorForLiquidGlassView(liquidGlassView: LiquidGlassView, context: Context) {
            if (android.os.Build.VERSION.SDK_INT >= 31) {
                val systemAccentDark = saturateColor(context.getColor(android.R.color.system_accent1_500), 0.6f)
                val systemAccentLight = context.getColor(android.R.color.system_accent1_200)
                liquidGlassView.glassTint = if (isDarkModeEnabled(context)) systemAccentDark else systemAccentLight
            }
        }

        fun setAccentColorForLiquidGlassView(liquidGlassView: org.teslasoft.assistant.ui.liquidglass.LiquidGlassView, context: Context) {
            if (android.os.Build.VERSION.SDK_INT >= 31) {
                val systemAccentDark = saturateColor(context.getColor(android.R.color.system_accent1_500), 0.6f)
                val systemAccentLight = context.getColor(android.R.color.system_accent1_200)
                liquidGlassView.glassTint = if (isDarkModeEnabled(context)) systemAccentDark else systemAccentLight
            }
        }

        // For fragments
        fun setAccentColorForLiquidGlassView(viewId: Int, parentView: View, context: Context) {
            val liquidGlassView = parentView.findViewById<View>(viewId)

            if (liquidGlassView is LiquidGlassView) {
                setAccentColorForLiquidGlassView(liquidGlassView, context)
            }

            if (liquidGlassView is org.teslasoft.assistant.ui.liquidglass.LiquidGlassView) {
                setAccentColorForLiquidGlassView(liquidGlassView, context)
            }
        }

        // For activities
        fun setAccentColorForLiquidGlassView(viewId: Int, activity: Activity) {
            setAccentColorForLiquidGlassView(viewId, activity.findViewById(android.R.id.content), activity)
        }


        // For fragments
        fun setAccentColorMutedForLiquidGlassView(viewId: Int, parentView: View, context: Context) {
            val liquidGlassView = parentView.findViewById<org.teslasoft.assistant.ui.liquidglass.LiquidGlassView>(viewId)
            if (android.os.Build.VERSION.SDK_INT >= 31) {
                liquidGlassView.glassTint = if (isDarkModeEnabled(context)) {
                    0x00FFFFFF
                } else {
                    brightenColor(saturateColor(context.getColor(R.color.accent_250), 0.6f), 2.2f)
                }
            }
        }

        // For activities
        fun setAccentColorMutedForLiquidGlassView(viewId: Int, activity: Activity) {
            setAccentColorMutedForLiquidGlassView(viewId, activity.findViewById(android.R.id.content), activity)
        }

        // For fragments
        fun scanForLiquidGlassAndInitSettings(parentView: View, context: Context, excludedViews: ArrayList<Int>? = arrayListOf(), tintMode: Boolean = true) {
            fun scanView(view: View) {
                if (view is LiquidGlassView) {
                    throw IllegalStateException("Fatal error: View with id ${view.id} is invalid type of LiquidGlassView. Please migrate to the new LiquidGlassView implementation: org.teslasoft.assistant.ui.liquidglass.LiquidGlassView")
                }

                if (view is org.teslasoft.assistant.ui.liquidglass.LiquidGlassView) {
                    if (view.id != View.NO_ID && !(excludedViews ?: arrayListOf()).contains(view.id)) {
                        initializeLiquidGlassViewById(view.id, parentView)

                        if (tintMode) {
                            setAccentColorForLiquidGlassView(view.id, parentView, context)
                        }
                    }
                }

                if (view is ViewGroup) {
                    for (i in 0 until view.childCount) {
                        scanView(view.getChildAt(i))
                    }
                }
            }

            scanView(parentView)
        }

        // For activities
        fun scanForLiquidGlassAndInitSettings(activity: Activity, excludedViews: ArrayList<Int>? = arrayListOf(), tintMode: Boolean = true) {
            scanForLiquidGlassAndInitSettings(activity.findViewById(android.R.id.content), activity, excludedViews, tintMode)
        }

        fun saturateColor(color: Int, saturation: Float): Int {
            val hsv = FloatArray(3)
            android.graphics.Color.colorToHSV(color, hsv)
            hsv[1] = saturation
            return android.graphics.Color.HSVToColor(hsv)
        }

        fun brightenColor(color: Int, factor: Float): Int {
            val r = (android.graphics.Color.red(color) * factor).coerceAtMost(255f).toInt()
            val g = (android.graphics.Color.green(color) * factor).coerceAtMost(255f).toInt()
            val b = (android.graphics.Color.blue(color) * factor).coerceAtMost(255f).toInt()
            return android.graphics.Color.rgb(r, g, b)
        }

        private fun isDarkModeEnabled(context: Context): Boolean {
            val uiMode = context.resources.configuration.uiMode
            val nightModeFlags = uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
            return nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES
        }

        fun dpF(v: Int, context: Context): Float = v * context.resources.displayMetrics.density
    }
}
