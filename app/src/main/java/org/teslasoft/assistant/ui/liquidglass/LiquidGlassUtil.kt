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
import com.example.liquidglass.LiquidGlassButton
import com.example.liquidglass.LiquidGlassView

@Suppress("unused")
class LiquidGlassUtil {
    companion object {
        // For fragments
        fun initializeLiquidGlassViewById(viewId: Int, parentView: View) {
            val liquidGlassView = parentView.findViewById<LiquidGlassView>(viewId)
            liquidGlassView.enableDynamicBackground = true
            liquidGlassView.edgeHighlightOpacity = 50.0f
            liquidGlassView.edgeHighlightBorderWidth = 4.0f
            liquidGlassView.blurAmount = 0.5f
            liquidGlassView.pressScale = 0.8f
        }

        // For activities
        fun initializeLiquidGlassViewById(viewId: Int, activity: Activity) {
            initializeLiquidGlassViewById(viewId, activity.findViewById(android.R.id.content))
        }

        // For fragments
        fun setAccentColorForLiquidGlassView(viewId: Int, parentView: View, context: Context) {
            val liquidGlassView = parentView.findViewById<LiquidGlassView>(viewId)
            if (android.os.Build.VERSION.SDK_INT >= 31) {
                val systemAccentDark = saturateColor(context.getColor(android.R.color.system_accent1_500), 0.6f)
                val systemAccentLight = context.getColor(android.R.color.system_accent1_200)
                liquidGlassView.glassTint = if (isDarkModeEnabled(context)) systemAccentDark else systemAccentLight
            }
        }

        // For activities
        fun setAccentColorForLiquidGlassView(viewId: Int, activity: Activity) {
            setAccentColorForLiquidGlassView(viewId, activity.findViewById(android.R.id.content), activity)
        }

        // For fragments
        fun scanForLiquidGlassAndInitSettings(parentView: View, context: Context, excludedViews: ArrayList<Int>? = arrayListOf(), tintMode: Boolean = true) {
            fun scanView(view: View) {
                if (view is LiquidGlassView) {
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

        private fun saturateColor(color: Int, saturation: Float): Int {
            val hsv = FloatArray(3)
            android.graphics.Color.colorToHSV(color, hsv)
            hsv[1] = saturation
            return android.graphics.Color.HSVToColor(hsv)
        }

        private fun isDarkModeEnabled(context: Context): Boolean {
            val uiMode = context.resources.configuration.uiMode
            val nightModeFlags = uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
            return nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES
        }
    }
}
