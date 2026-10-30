/**
 * Modified and distributed under the MIT License.
 *
 * Original author: QWEA0 (https://github.com/QWEA0)
 * Source: https://github.com/QWEA0/Liquid-Glass-Android/blob/main/liquidglass/src/main/java/com/example/liquidglass/HardwareBackdropBlur.kt
 *
 * Modified by: AndraxDev (https://github.com/AndraxDev)
 *
 * Modifications include:
 * - Experimental version
 * - Adopted to the latest Kotlin version
 * */

package org.teslasoft.assistant.ui.liquidglass

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Path
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.RuntimeColorFilter
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.graphics.withClip

internal class HardwareBackdropBlur {

    companion object {
        /** 色差 RuntimeShader 需要 API 33+ */
        fun supportsRuntimeShader(): Boolean = true

        /**
         * 色差 AGSL 着色器
         *
         * 与 CPU 实现对应关系：
         * - baseDx = (mapR - 128) * scale / 255  ≈  (m.r - 0.5) * displacementScale
         * - 每个通道在 x、y 上都加上自己的偏移量（channelOffsets 已含 intensity）
         * - alpha 取原位置的值
         */
        private const val ABERRATION_AGSL = """
            uniform shader content;
            uniform shader dispMap;
            uniform float2 dispCoordScale;
            uniform float displacementScale;
            uniform float3 channelOffsets;

            half4 main(float2 coord) {
                half4 m = dispMap.eval(coord * dispCoordScale);
                float2 base = float2((float(m.r) - 0.5) * displacementScale,
                                     (float(m.g) - 0.5) * displacementScale);
                // 通道偏移沿位移方向应用（标量直接相加会广播成 45° 对角偏移）
                float bl = length(base);
                float2 dir = float2(0.0);
                if (bl > 0.001) {
                    dir = base / bl;
                }
                half r = content.eval(coord + base + dir * channelOffsets.x).r;
                half g = content.eval(coord + base + dir * channelOffsets.y).g;
                half b = content.eval(coord + base + dir * channelOffsets.z).b;
                half a = content.eval(coord).a;
                return half4(r, g, b, a);
            }
        """
    }

    /**
     * 色差参数（null = 不启用色差）
     *
     * @param displacementMap 位移贴图（R=dx, G=dy，128 为零位移）
     * @param displacementScale 位移缩放（overLight 时调用方应减半）
     * @param redOffset 红色通道偏移（已乘 intensity）
     * @param greenOffset 绿色通道偏移（已乘 intensity）
     * @param blueOffset 蓝色通道偏移（已乘 intensity）
     */
    data class AberrationParams(
        val displacementMap: Bitmap,
        val displacementScale: Float,
        val redOffset: Float,
        val greenOffset: Float,
        val blueOffset: Float
    )

    private val renderNode = RenderNode("LiquidGlassBackdrop")

    /** 调试用 API 级别钳制（见 LiquidGlassView.debugApiLevelCap；影响 vibrancy 选择） */
    var debugApiLevelCap: Int = Int.MAX_VALUE

    // 仅参数变化时重建 RenderEffect
    private var lastBlurRadius = Float.NaN
    private var lastSaturation = Float.NaN
    private var lastAberration: AberrationParams? = null
    private var lastWidth = 0
    private var lastHeight = 0
    private var lastApiCap = Int.MAX_VALUE
    private var effectBuilt = false

    private var aberrationShader: RuntimeShader? = null

    // API 36+ vibrancy 饱和度滤镜（非线性；不支持时回退 ColorMatrix）
    private var vibrancyFilter: RuntimeColorFilter? = null

    private val location = IntArray(2)
    private val parentLocation = IntArray(2)

    private val backdropCapture = BackdropCapture()

    /**
     * 绘制处理后的背景到目标画布
     *
     * @param canvas 目标画布（必须硬件加速）
     * @param glassView 玻璃视图（用于定位与跳过自绘）
     * @param blurRadius 模糊半径（像素，0 表示不模糊）
     * @param saturation 饱和度（100 = 原始）
     * @param clipPath 圆角裁剪路径
     * @param aberration 色差参数（null = 不启用；需 API 33+，否则忽略并返回 false）
     * @return 是否成功绘制；false 时调用方应回退到 CPU 管线
     */
    fun draw(
        canvas: Canvas,
        glassView: LiquidGlassView,
        blurRadius: Float,
        saturation: Float,
        clipPath: Path,
        aberration: AberrationParams? = null
    ): Boolean {
        if (!canvas.isHardwareAccelerated) return false
        if (aberration != null && !supportsRuntimeShader()) return false
        // 背景来源：默认直接父容器，setBackdropSource 后为指定视图（可跨层级）
        val parent = glassView.backdropView ?: return false
        val width = glassView.width
        val height = glassView.height
        if (width <= 0 || height <= 0) return false

        if (!effectBuilt ||
            blurRadius != lastBlurRadius ||
            saturation != lastSaturation ||
            aberration != lastAberration ||
            width != lastWidth ||
            height != lastHeight ||
            debugApiLevelCap != lastApiCap
        ) {
            renderNode.setRenderEffect(buildEffect(blurRadius, saturation, aberration, width, height))
            lastBlurRadius = blurRadius
            lastSaturation = saturation
            lastAberration = aberration
            lastWidth = width
            lastHeight = height
            lastApiCap = debugApiLevelCap
            effectBuilt = true
        }

        // 计算相对背景视图的偏移（用屏幕坐标差，兼容滚动容器与跨 window 的背景视图）
        glassView.getLocationOnScreen(location)
        parent.getLocationOnScreen(parentLocation)
        val offsetX = (location[0] - parentLocation[0]).toFloat()
        val offsetY = (location[1] - parentLocation[1]).toFloat()

        renderNode.setPosition(0, 0, width, height)
        val recordingCanvas = renderNode.beginRecording(width, height)
        try {
            recordingCanvas.translate(-offsetX, -offsetY)
            glassView.isCapturingBackdrop = true
            // ⚠️ 硬件画布上父视图绘制子视图走的是 updateDisplayListIfDirty，
            // 不会经过 View.draw(Canvas)，所以 isCapturingBackdrop 拦截不到自己，
            // 会对正在录制中的 RenderNode 重入 beginRecording 导致崩溃。
            // 跳过自身、以及跨层级祖先的重入/成环处理都在 BackdropCapture 里。
            try {
                backdropCapture.draw(recordingCanvas, parent, glassView)
            } finally {
                glassView.isCapturingBackdrop = false
            }
        } finally {
            renderNode.endRecording()
        }

        canvas.withClip(clipPath) {
            canvas.drawRenderNode(renderNode)
        }
        return true
    }

    /**
     * 构建效果链：模糊 → 色差 → 饱和度（内层先执行）
     */
    private fun buildEffect(
        blurRadius: Float,
        saturation: Float,
        aberration: AberrationParams?,
        width: Int,
        height: Int
    ): RenderEffect? {
        var effect: RenderEffect? = if (blurRadius > 0f) {
            RenderEffect.createBlurEffect(blurRadius, blurRadius, Shader.TileMode.CLAMP)
        } else {
            null
        }

        if (aberration != null && supportsRuntimeShader()) {
            val aberrationEffect = buildAberrationEffect(aberration, width, height)
            effect = if (effect != null) {
                RenderEffect.createChainEffect(aberrationEffect, effect)
            } else {
                aberrationEffect
            }
        }

        if (saturation != 100f) {
            val satEffect = RenderEffect.createColorFilterEffect(
                saturationFilter(saturation / 100f)
            )
            effect = if (effect != null) {
                RenderEffect.createChainEffect(satEffect, effect)
            } else {
                satEffect
            }
        }

        return effect
    }

    /**
     * 饱和度滤镜：API 36+ 用 vibrancy（非线性，AGSL 颜色滤镜），
     * 以下版本回退线性 ColorMatrix。buildEffect 仅在参数变化时调用，
     * uniform 在 createColorFilterEffect 时被快照，复用实例安全。
     */
    private fun saturationFilter(factor: Float): ColorFilter {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA &&
            debugApiLevelCap >= Build.VERSION_CODES.BAKLAVA
        ) {
            val vib = vibrancyFilter ?: GlassRuntimeEffects.createVibrancyFilter()?.also { vibrancyFilter = it }
            if (vib != null) {
                vib.setFloatUniform("satFactor", factor)
                return vib
            }
        }
        return ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(factor) })
    }

    private fun buildAberrationEffect(params: AberrationParams, width: Int, height: Int): RenderEffect {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val shader =
                aberrationShader ?: RuntimeShader(ABERRATION_AGSL).also { aberrationShader = it }

            shader.setInputShader(
                "dispMap",
                BitmapShader(params.displacementMap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            )
            // 位移贴图尺寸可能与视图不同（生成滞后一帧），按比例映射采样坐标
            shader.setFloatUniform(
                "dispCoordScale",
                params.displacementMap.width.toFloat() / width,
                params.displacementMap.height.toFloat() / height
            )
            shader.setFloatUniform("displacementScale", params.displacementScale)
            shader.setFloatUniform(
                "channelOffsets",
                params.redOffset,
                params.greenOffset,
                params.blueOffset
            )

            return RenderEffect.createRuntimeShaderEffect(shader, "content")
        } else {
            // ColorMatrix

            val cm = ColorMatrix(
                floatArrayOf(
                    1.4f, 0f, 0f, 0f, params.redOffset,
                    0f, 1.4f, 0f, 0f, params.greenOffset,
                    0f, 0f, 1.4f, 0f, params.blueOffset,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            return RenderEffect.createColorFilterEffect(ColorMatrixColorFilter(cm))
        }
    }

    fun release() {
        renderNode.discardDisplayList()
        aberrationShader = null
    }
}
