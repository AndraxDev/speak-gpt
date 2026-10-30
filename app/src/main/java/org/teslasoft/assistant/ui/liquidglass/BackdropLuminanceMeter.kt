/**
 * Modified and distributed under the MIT License.
 *
 * Original author: QWEA0 (https://github.com/QWEA0)
 * Source: https://github.com/QWEA0/Liquid-Glass-Android/blob/main/liquidglass/src/main/java/com/example/liquidglass/BackdropLuminanceMeter.kt
 *
 * Modified by: AndraxDev (https://github.com/AndraxDev)
 *
 * Modifications include:
 * - Experimental version
 * - Adopted to the latest Kotlin version
 * */

package org.teslasoft.assistant.ui.liquidglass

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.core.graphics.get
import androidx.core.graphics.createBitmap

internal class BackdropLuminanceMeter(
    private val view: LiquidGlassView,
    private val onLuminance: (Float) -> Unit
) {
    companion object {
        private const val SAMPLE_SIZE = 24
        private const val EMA_ALPHA = 0.35f
        private const val OVER_LIGHT_ON = 0.60f
        private const val OVER_LIGHT_OFF = 0.45f

        /**
         * 稀疏采样一张已有位图的平均亮度（CPU 管线复用背景位图时调用）
         */
        fun measureBitmap(bitmap: Bitmap): Float {
            val w = bitmap.width
            val h = bitmap.height
            if (w <= 0 || h <= 0 || bitmap.isRecycled) return 0.5f
            val grid = 8
            val stepX = (w / grid).coerceAtLeast(1)
            val stepY = (h / grid).coerceAtLeast(1)
            var sum = 0f
            var count = 0
            var y = stepY / 2
            while (y < h) {
                var x = stepX / 2
                while (x < w) {
                    val c = bitmap[x, y]
                    sum += 0.2126f * Color.red(c) + 0.7152f * Color.green(c) + 0.0722f * Color.blue(c)
                    count++
                    x += stepX
                }
                y += stepY
            }
            return if (count > 0) (sum / count / 255f).coerceIn(0f, 1f) else 0.5f
        }
    }

    var intervalMs = 350L

    /** EMA 平滑后的亮度（0 = 全黑，1 = 全白） */
    var smoothedLuminance = 0.5f
        private set

    /** 当前是否判定为亮背景（带滞回） */
    var isOverLight = false
        private set

    private var running = false
    private var sampleBitmap: Bitmap? = null
    private val location = IntArray(2)
    private val parentLocation = IntArray(2)

    private val tick = object : Runnable {
        override fun run() {
            if (!running) return
            sampleFromParent()
            view.postDelayed(this, intervalMs)
        }
    }

    fun start() {
        if (running) return
        running = true
        view.postDelayed(tick, intervalMs)
    }

    fun stop() {
        running = false
        view.removeCallbacks(tick)
        sampleBitmap?.recycle()
        sampleBitmap = null
    }

    /**
     * GPU 管线的采样路径：把父视图缩绘到 24×24 位图上估算亮度
     */
    private fun sampleFromParent() {
        // 与捕获路径取同一个背景来源，否则指定 backdropSource 后染色会读错内容
        val parent = view.backdropView ?: return
        val w = view.width
        val h = view.height
        if (w <= 0 || h <= 0) return

        val bmp = sampleBitmap?.takeIf { !it.isRecycled }
            ?: createBitmap(SAMPLE_SIZE, SAMPLE_SIZE).also { sampleBitmap = it }
        bmp.eraseColor(Color.GRAY)

        try {
            val canvas = Canvas(bmp)
            view.getLocationOnScreen(location)
            parent.getLocationOnScreen(parentLocation)
            val offsetX = (location[0] - parentLocation[0]).toFloat()
            val offsetY = (location[1] - parentLocation[1]).toFloat()

            // 非等比缩放到 24×24：亮度估算不需要保持纵横比
            canvas.scale(SAMPLE_SIZE.toFloat() / w, SAMPLE_SIZE.toFloat() / h)
            canvas.translate(-offsetX, -offsetY)

            view.isCapturingBackdrop = true
            LiquidGlassView.isLuminanceSampling = true
            try {
                parent.draw(canvas)
            } finally {
                LiquidGlassView.isLuminanceSampling = false
                view.isCapturingBackdrop = false
            }
        } catch (_: Exception) {
            return
        }

        submit(measureBitmap(bmp))
    }

    /**
     * 提交一个亮度样本（CPU 管线用 [measureBitmap] 算好后从这里进）
     */
    fun submit(luminance: Float) {
        smoothedLuminance += (luminance - smoothedLuminance) * EMA_ALPHA

        // 带滞回的亮背景判定
        if (!isOverLight && smoothedLuminance > OVER_LIGHT_ON) {
            isOverLight = true
        } else if (isOverLight && smoothedLuminance < OVER_LIGHT_OFF) {
            isOverLight = false
        }

        onLuminance(smoothedLuminance)
    }
}
