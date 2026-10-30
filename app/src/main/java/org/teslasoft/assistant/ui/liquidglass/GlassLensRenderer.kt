/**
 * Modified and distributed under the MIT License.
 *
 * Original author: QWEA0 (https://github.com/QWEA0)
 * Source: https://github.com/QWEA0/Liquid-Glass-Android/blob/main/liquidglass/src/main/java/com/example/liquidglass/GlassLensRenderer.kt
 *
 * Modified by: AndraxDev (https://github.com/AndraxDev)
 *
 * Modifications include:
 * - Experimental version
 * - Adopted to the latest Kotlin version
 * - Soften corners of rectangle shapes
 * */


package org.teslasoft.assistant.ui.liquidglass

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
internal class GlassLensRenderer {

    companion object {
        private const val TAG = "GlassLensRenderer"

        @SuppressLint("ObsoleteSdkInt")
        fun isSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

        /**
         * 透镜 AGSL 着色器
         *
         * 坐标约定：
         * - coord 为录制内容（含 margin 外扩）的像素坐标
         * - p = coord - margin 为视图局部坐标，形状参数均在此空间
         */
        private const val LENS_AGSL = """
            uniform shader content;
            uniform float  margin;
            uniform float2 viewSize;
            uniform float4 shape1;    // 真实主形状（触摸凸起的尺度用它）
            uniform float4 radii1;
            uniform float4 shape1L;   // 主形状：平边方向延伸到视图外，那条边就没有斜面（视图矩形外硬切）
            uniform float  rimSoft;   // 边缘柔化：折射带内沿法线方向的抹匀宽度（px，0 = 关）
            uniform float4 shape2;
            uniform float  radius2;
            uniform float  blendK;
            uniform float  bevel;
            uniform float  refractPx;
            uniform float  falloff;      // 折射剖面：> 0 为逆幂（引力透镜）衰减指数，0 = 平方斜面
            uniform float  refractDir;   // -1 向内采样（默认，与 iOS 一致：内侧压缩镜像）/ +1 向外（可选的凸透镜模式）
            uniform float2 sampleLo;     // 采样安全区（录制内容坐标）：区外没有内容，读到的是透明黑
            uniform float2 sampleHi;
            uniform float  dispersion;
            uniform float2 lightDir;
            uniform float  specStrength;
            uniform float  rimBandMax;   // 贴边高光带宽度上限（px，小控件按短边收）
            uniform float4 tintColor;
            uniform float  adaptiveTint;
            uniform float4 glassTint;
            uniform float  dimAmount;
            uniform float  satFactor;
            uniform float  press;
            uniform float2 touchPos;
            uniform float  touchAmp;

            float sdRoundedBox(float2 p, float2 b, float r) {
                float maxR = min(b.x, b.y);
                r = clamp(r, 0.0, maxR);
            
                // How close this is to a full capsule/pill radius.
                float radiusRatio = r / max(maxR, 0.0001);
            
                // Strong continuous-corner character for normal rectangles,
                // progressively rounder for large radii / pills.
                //
                // Normal corners: ~4.2
                // Full pill:      ~2.4
                float n = mix(
                    3.2,
                    2.6,
                    smoothstep(0.55, 1.0, radiusRatio)
                );
            
                float2 q = abs(p) - b + r;
                float2 u = max(q, float2(0.0));
            
                float corner = pow(
                    pow(u.x, n) +
                    pow(u.y, n),
                    1.0 / n
                );
            
                return corner + min(max(q.x, q.y), 0.0) - r;
            }

            // r = (top-left, top-right, bottom-right, bottom-left)
            float sdRoundedBox4(float2 p, float2 b, float4 r) {
                float rx =
                    (p.x > 0.0)
                        ? ((p.y > 0.0) ? r.z : r.y)
                        : ((p.y > 0.0) ? r.w : r.x);
            
                float maxR = min(b.x, b.y);
                rx = clamp(rx, 0.0, maxR);
            
                float radiusRatio = rx / max(maxR, 0.0001);
            
                // Same adaptive continuous-corner profile.
                //
                // Smaller radius:
                //     stronger Apple-style smoothing.
                //
                // Near-pill radius:
                //     moves closer to a circle so the radius remains visually
                //     meaningful and the ends stay properly rounded.
                float n = mix(
                    3.2,
                    2.6,
                    smoothstep(0.55, 1.0, radiusRatio)
                );
            
                float2 q = abs(p) - b + rx;
                float2 u = max(q, float2(0.0));
            
                float corner = pow(
                    pow(u.x, n) +
                    pow(u.y, n),
                    1.0 / n
                );
            
                return corner + min(max(q.x, q.y), 0.0) - rx;
            }

            float sminPoly(float a, float b, float k) {
                float h = clamp(0.5 + 0.5 * (b - a) / k, 0.0, 1.0);
                return mix(b, a, h) - k * h * (1.0 - h);
            }

            // 斜面 / 法线 / 高光用透镜形状（平边已延伸出去，不产生边缘）
            float lensSDF(float2 p) {
                float d = sdRoundedBox4(p - shape1L.xy, shape1L.zw, radii1);
                if (shape2.z > 0.5) {
                    float d2 = sdRoundedBox(p - shape2.xy, shape2.zw, radius2);
                    if (blendK > 0.5) {
                        d = sminPoly(d, d2, blendK);
                    } else {
                        d = min(d, d2);
                    }
                }
                return d;
            }

            half4 main(float2 coord) {
                float2 p = coord - float2(margin, margin);
                // 视图矩形外硬切：平边方向形状延伸到了视图外，靠这一刀收口，
                // 平边上不做羽化，两块玻璃贴边拼接时不会叠出一条发丝缝
                if (p.x < 0.0 || p.y < 0.0 || p.x > viewSize.x || p.y > viewSize.y) {
                    return half4(0.0);
                }
                float d = lensSDF(p);

                // 覆盖率：1.5px 抗锯齿羽化，形状外完全透明
                float cov = clamp(0.5 - d / 1.5, 0.0, 1.0);
                if (cov <= 0.004) {
                    return half4(0.0);
                }

                // SDF 数值梯度 → 屏幕空间外法线
                float2 n = float2(
                    lensSDF(p + float2(1.0, 0.0)) - lensSDF(p - float2(1.0, 0.0)),
                    lensSDF(p + float2(0.0, 1.0)) - lensSDF(p - float2(0.0, 1.0))
                );
                float nLen = length(n);
                if (nLen > 0.0001) {
                    n = n / nLen;
                } else {
                    n = float2(0.0, -1.0);
                }

                // 厚度剖面：t=1 平坦内部，t=0 边缘；slope 为斜面陡峭度（折射位移的比例）
                float t = clamp(-d / max(bevel, 1.0), 0.0, 1.0);
                float edge = 1.0 - t;
                float slope;
                if (falloff > 0.001) {
                    // 引力透镜式逆幂衰减：位移 ∝ (1 + x/k)^-p，核半径 k = 斜面宽度的 1/4，
                    // 减去带末端的值再归一化，贴边 = 1、带末端平滑落到 0。越贴边越剧烈：
                    // p = 2 时离边 k 处只剩 1/4，绝大部分弯折压在最外几个像素，
                    // 内侧只留一段缓慢回落的轻微放大尾巴
                    float gB = pow(5.0, -falloff);
                    slope = (pow(1.0 + 4.0 * t, -falloff) - gB) / (1.0 - gB);
                } else {
                    // 平方斜面：弯折沿整条斜面带均匀铺开
                    slope = edge * edge;
                }

                // 折射：refractDir = -1 沿法线向内采样（默认，与 iOS 一致）——边缘是内侧背景的
                // 压缩镜像；+1 为可选的凸透镜模式，向外采样，形状外的背景被弯进边缘，
                // 靠近的内容还没进到玻璃下面就先出现在边缘，进来之后沿边缘延展。
                // RuntimeShader 子输入只保证"输出裁剪区"内可采样（Android 未暴露 Skia 的
                // childSampleRadius），向外采样必须让输出区覆盖整个外扩录制区——见 draw()
                // 里的外层合成节点；采样再由 sampleLo/Hi 钳在有内容的范围内
                float refr = refractPx * (1.0 + 0.6 * press);
                float2 offset = n * (refractDir * slope * refr);

                // 触摸局部液态凸起（手指下方的局部放大：采样向触点收缩）
                if (touchAmp > 0.001) {
                    float2 tp = p - touchPos;
                    float tr = length(tp);
                    float sigma = max(max(shape1.z, shape1.w), 1.0);
                    float bump = touchAmp * exp(-(tr * tr) / (sigma * sigma * 0.30));
                    if (tr > 1.0) {
                        offset -= (tp / tr) * (bump * refr * 0.5);
                    }
                }

                // 色散：三通道折射量不同（蓝光弯折最多），边缘出现光谱边纹
                float2 cR = coord + offset * (1.0 - dispersion * slope);
                float2 cG = coord + offset;
                float2 cB = coord + offset * (1.0 + dispersion * slope);

                // 安全钳制到有内容的采样区，杜绝透明黑
                float2 lo = sampleLo;
                float2 hi = sampleHi;
                cR = clamp(cR, lo, hi);
                cG = clamp(cG, lo, hi);
                cB = clamp(cB, lo, hi);
                float3 col;
                if (rimSoft > 0.01 && slope > 0.001) {
                    // 边缘柔化：折射带内沿法线方向抹匀（宽度随斜面深度增长），
                    // 压缩带从一条硬线变成一段渐变，采样点仍钳在内容区内
                    float2 sm = n * (rimSoft * slope);
                    float2 cR1 = clamp(cR - sm, lo, hi);
                    float2 cR2 = clamp(cR + sm, lo, hi);
                    float2 cG1 = clamp(cG - sm, lo, hi);
                    float2 cG2 = clamp(cG + sm, lo, hi);
                    float2 cB1 = clamp(cB - sm, lo, hi);
                    float2 cB2 = clamp(cB + sm, lo, hi);
                    col = float3(
                        (content.eval(cR).r + content.eval(cR1).r + content.eval(cR2).r) / 3.0,
                        (content.eval(cG).g + content.eval(cG1).g + content.eval(cG2).g) / 3.0,
                        (content.eval(cB).b + content.eval(cB1).b + content.eval(cB2).b) / 3.0
                    );
                } else {
                    col = float3(
                        content.eval(cR).r,
                        content.eval(cG).g,
                        content.eval(cB).b
                    );
                }

                // 饱和度（合并进同一 pass）；提饱和端走 vibrancy 曲线：低饱和
                // 像素多提、高饱和像素少提、极亮像素保护，避免线性提饱和把浓色
                // 推过曝（曲线与 GlassRuntimeEffects 的 API 36 滤镜一致）
                float lum = dot(col, float3(0.2126, 0.7152, 0.0722));
                if (satFactor <= 1.0) {
                    col = mix(float3(lum), col, satFactor);
                } else {
                    float satNow = max(col.r, max(col.g, col.b)) - min(col.r, min(col.g, col.b));
                    float room = 1.0 - smoothstep(0.2, 0.85, satNow);
                    float hl = 1.0 - smoothstep(0.75, 0.98, lum);
                    float amount = 1.0 + (satFactor - 1.0) * mix(0.3, 1.0, room * hl);
                    col = clamp(mix(float3(lum), col, amount), float3(0.0), float3(1.0));
                }

                // 自适应染色（Regular）/ 压暗层（Clear）
                if (adaptiveTint > 0.5) {
                    // 逐像素自适应：按局部（模糊后）亮度在提亮/压暗之间平滑过渡，
                    // 玻璃跨明暗背景时不再整体翻转；曲线与全局版本
                    // （LiquidGlassView.onLuminanceSample）一致
                    float lumT = dot(col, float3(0.2126, 0.7152, 0.0722));
                    float e = smoothstep(0.35, 0.75, lumT);
                    col = mix(col, float3(1.0 - e), 0.14 + 0.08 * e);
                } else {
                    col = mix(col, tintColor.rgb, tintColor.a);
                }
                col = col * (1.0 - dimAmount);

                // 使用方指定的玻璃本体色：按"有色介质"建模——吸收（保留背景明暗
                // 层次与折射细节）+ 少量散射（暗背景下也看得出色相）。
                // 位置在光照之前：染色属于透射，镜面高光属于表面反射，不该被染色
                if (glassTint.a > 0.002) {
                    float lumTint = dot(col, float3(0.2126, 0.7152, 0.0722));
                    float3 absorbed = col * mix(float3(1.0), glassTint.rgb, 0.85);
                    float3 scattered = glassTint.rgb * (0.38 * (1.0 - lumTint));
                    col = mix(col, clamp(absorbed + scattered, float3(0.0), float3(1.0)), glassTint.a);
                }

                // 光照：同一法线场驱动。整圈亮边的明暗只由 dot(N, -L) 决定，没有与方向
                // 无关的常亮项——侧向（法线垂直于光线处）归零，不会留下一圈固定描边。
                // 两道对称的角度瓣（沿 iOS 26 控制中心截图里的控件一圈逐角量得）：迎光侧
                // 与背光侧峰值相等（背光侧是透明介质的内壁反射），瓣宽 pow 4.5（离轴 30°
                // 剩一半、45° 归零）；迎光侧另加一层向内的柔和辉光——iOS 左上角是亮线 +
                // 辉光，右下角只有亮线，两侧的边缘内侧都没有暗带
                float facing = dot(n, -lightDir);
                float lobeF = pow(max(facing, 0.0), 4.5);
                float lobeB = pow(max(-facing, 0.0), 4.5);

                // 贴边亮线（中心在边内 1px，半宽 2px）+ 迎光侧辉光：从亮线内侧（边内 3px）起
                // 向内 1.5 次幂衰减，宽度与斜面弱相关、上限默认 6px、小控件按短边收。
                // 辉光不叠在亮线上，两侧亮线峰值相等
                float bandW = clamp(bevel * 0.3, 2.0, rimBandMax);
                float glowIn = clamp((-d - 1.0) / 2.0, 0.0, 1.0);
                float glow = glowIn * pow(clamp(1.0 - (-d - 3.0) / bandW, 0.0, 1.0), 1.5) * cov;
                float hair = clamp(1.0 - abs(d + 1.0) / 2.0, 0.0, 1.0) * cov;
                float spec = (hair * 0.70 * (lobeF + lobeB) + glow * 0.10 * lobeF)
                             * specStrength * (1.0 - 0.35 * press);
                col += float3(spec);

                col = clamp(col, float3(0.0), float3(1.0));
                return half4(half3(col * cov), half(cov));
            }
        """
    }

    /**
     * 透镜渲染参数（值语义；与上一帧不等时才重建 RenderEffect）
     *
     * 形状坐标均为视图局部像素；shape2HW <= 0 表示副形状禁用。
     */
    data class LensParams(
        val blurRadius: Float,
        val shape1CX: Float, val shape1CY: Float,
        val shape1HW: Float, val shape1HH: Float,
        val radius1TL: Float, val radius1TR: Float, val radius1BR: Float, val radius1BL: Float,
        // 透镜形状（平边方向延伸后的主形状；无平边时与 shape1 相同）
        val lens1CX: Float, val lens1CY: Float, val lens1HW: Float, val lens1HH: Float,
        val rimSoft: Float,     // 边缘柔化宽度（px）
        val shape2CX: Float, val shape2CY: Float,
        val shape2HW: Float, val shape2HH: Float, val radius2: Float,
        val blendK: Float,
        val bevel: Float,
        val refract: Float,
        val falloff: Float,     // 折射剖面：> 0 逆幂衰减指数，0 = 平方斜面
        val outward: Boolean,   // 折射向外采样（true）/ 向内（旧行为）
        val rimBandMax: Float,  // 迎光侧辉光带宽度上限（px）
        val dispersion: Float,
        val lightX: Float, val lightY: Float,
        val spec: Float,
        val tint: Int,          // straight-alpha ARGB（adaptiveTint 时被忽略）
        val adaptiveTint: Boolean, // 逐像素自适应染色（Regular + enableAdaptiveTint）
        val glassTint: Int,     // 使用方指定的玻璃本体色（straight-alpha ARGB，a=0 关闭）
        val dim: Float,
        val saturation: Float,  // 100 = 原始
        val press: Float,
        val touchX: Float, val touchY: Float, val touchAmp: Float
    )

    private val renderNode = RenderNode("LiquidGlassLens")

    /**
     * 向外折射用的外层合成节点。RenderEffect 的输出区是节点范围与画布裁剪区的交集，
     * 带效果的节点直接画到视图画布上会被裁到视图矩形，margin 里录下的内容对着色器
     * 不可见（RuntimeShader 子输入只保证输出区内可采样）。把它画进一个自带合成层、
     * 范围等于整个录制区的外层节点：层内裁剪区就是录制区，着色器采得到 margin；
     * 外层节点再画到视图画布上时才被裁到视图矩形
     */
    private val layerNode = RenderNode("LiquidGlassLensLayer").apply {
        setUseCompositingLayer(true, null)
    }

    // 采样安全区（录制内容坐标），随玻璃相对背景来源的位置变化
    private var sampleLoX = 0f
    private var sampleLoY = 0f
    private var sampleHiX = 0f
    private var sampleHiY = 0f

    private var shader: RuntimeShader? = null
    private var shaderBroken = false

    private var lastParams: LensParams? = null
    private var lastWidth = 0
    private var lastHeight = 0
    private var lastMargin = -1

    private val location = IntArray(2)
    private val parentLocation = IntArray(2)

    private val backdropCapture = BackdropCapture()

    /** AGSL 编译失败（个别设备驱动问题）时为 false，调用方应回退旧管线 */
    val isAvailable: Boolean
        get() = !shaderBroken

    /**
     * 绘制透镜玻璃到目标画布
     *
     * @param canvas 目标画布（必须硬件加速）
     * @param glassView 玻璃视图（定位 + 录制时跳过自身）
     * @param params 本帧渲染参数
     * @param margin 背景录制外扩边距（px，覆盖折射/模糊采样越界）
     * @return 是否成功绘制；false 时调用方应回退
     */
    fun draw(
        canvas: Canvas,
        glassView: LiquidGlassView,
        params: LensParams,
        margin: Int
    ): Boolean {
        if (shaderBroken) return false
        if (!canvas.isHardwareAccelerated) return false
        // 背景来源：默认直接父容器，setBackdropSource 后为指定视图（可跨层级）
        val parent = glassView.backdropView ?: return false
        val width = glassView.width
        val height = glassView.height
        if (width <= 0 || height <= 0) return false

        val sh = shader ?: try {
            RuntimeShader(LENS_AGSL).also { shader = it }
        } catch (e: Exception) {
            // AGSL 编译失败：标记不可用，让调用方永久回退旧管线
            Log.e(TAG, "Lens AGSL compile failed, falling back", e)
            shaderBroken = true
            return false
        }

        // 计算相对背景视图的偏移（屏幕坐标差：兼容滚动容器，且背景视图在
        // 另一个 window（Dialog/PopupWindow）时也成立）
        glassView.getLocationOnScreen(location)
        parent.getLocationOnScreen(parentLocation)
        val offsetX = (location[0] - parentLocation[0]).toFloat()
        val offsetY = (location[1] - parentLocation[1]).toFloat()

        val recW = width + 2 * margin
        val recH = height + 2 * margin
        val boundsChanged = updateSampleBounds(
            params.outward, margin, width, height, recW, recH,
            offsetX, offsetY, parent.width, parent.height
        )

        if (boundsChanged || params != lastParams ||
            width != lastWidth || height != lastHeight || margin != lastMargin
        ) {
            try {
                renderNode.setRenderEffect(buildEffect(sh, params, margin, width, height))
            } catch (e: Exception) {
                Log.e(TAG, "Lens effect build failed, falling back", e)
                shaderBroken = true
                return false
            }
            lastParams = params
            lastWidth = width
            lastHeight = height
            lastMargin = margin
        }

        if (params.outward) {
            // 见 layerNode 的说明：带效果的节点画进外层合成节点，让着色器的输出区覆盖整个录制区
            renderNode.setPosition(0, 0, recW, recH)
            recordBackdrop(parent, glassView, offsetX, offsetY, margin, recW, recH)
            layerNode.setPosition(-margin, -margin, width + margin, height + margin)
            val layerCanvas = layerNode.beginRecording(recW, recH)
            try {
                layerCanvas.drawRenderNode(renderNode)
            } finally {
                layerNode.endRecording()
            }
            canvas.drawRenderNode(layerNode)
        } else {
            // 录制区域向四周外扩 margin：模糊在边缘处能采到真实内容而非透明黑
            renderNode.setPosition(-margin, -margin, width + margin, height + margin)
            recordBackdrop(parent, glassView, offsetX, offsetY, margin, recW, recH)
            // 形状覆盖率由着色器输出（形状外 alpha=0），无需 clipPath
            canvas.drawRenderNode(renderNode)
        }
        return true
    }

    /** 把背景来源画进 renderNode（节点局部坐标，原点在外扩后的录制区左上角） */
    private fun recordBackdrop(
        parent: View,
        glassView: LiquidGlassView,
        offsetX: Float,
        offsetY: Float,
        margin: Int,
        recW: Int,
        recH: Int
    ) {
        val recordingCanvas = renderNode.beginRecording(recW, recH)
        try {
            recordingCanvas.translate(margin - offsetX, margin - offsetY)
            glassView.isCapturingBackdrop = true
            // 硬件画布上父视图绘制子视图不走 View.draw，isCapturingBackdrop 拦不住
            // 自己；跳过自身、以及跨层级祖先的重入/成环处理都在 BackdropCapture 里
            try {
                backdropCapture.draw(recordingCanvas, parent, glassView)
            } finally {
                glassView.isCapturingBackdrop = false
            }
        } finally {
            renderNode.endRecording()
        }
    }

    /**
     * 采样安全区。向内采样时是视图矩形（旧行为）；向外采样时是整个录制区与背景来源
     * 矩形的交集——来源之外没有内容，读到的是透明黑。滚动时来源边界逐帧移动，量化到
     * 4px 免得每帧重建 effect；玻璃离来源边缘超过 margin 时安全区恒为整个录制区
     *
     * @return 安全区是否变化（变化则需重建 effect）
     */
    private fun updateSampleBounds(
        outward: Boolean,
        margin: Int,
        width: Int,
        height: Int,
        recW: Int,
        recH: Int,
        offsetX: Float,
        offsetY: Float,
        parentW: Int,
        parentH: Int
    ): Boolean {
        val loX: Float
        val loY: Float
        val hiX: Float
        val hiY: Float
        if (outward) {
            val pl = margin - offsetX
            val pt = margin - offsetY
            loX = max(1f, floor(max(1f, pl + 1f) / 4f) * 4f)
            loY = max(1f, floor(max(1f, pt + 1f) / 4f) * 4f)
            hiX = max(loX, min(recW - 1f, ceil(min(recW - 1f, pl + parentW - 1f) / 4f) * 4f))
            hiY = max(loY, min(recH - 1f, ceil(min(recH - 1f, pt + parentH - 1f) / 4f) * 4f))
        } else {
            loX = margin + 1f
            loY = margin + 1f
            hiX = margin + width - 1f
            hiY = margin + height - 1f
        }
        val changed = loX != sampleLoX || loY != sampleLoY || hiX != sampleHiX || hiY != sampleHiY
        sampleLoX = loX
        sampleLoY = loY
        sampleHiX = hiX
        sampleHiY = hiY
        return changed
    }

    /**
     * 构建效果链：模糊（内层）→ 透镜着色器（外层）
     */
    private fun buildEffect(
        sh: RuntimeShader,
        p: LensParams,
        margin: Int,
        width: Int,
        height: Int
    ): RenderEffect {
        sh.setFloatUniform("margin", margin.toFloat())
        sh.setFloatUniform("viewSize", width.toFloat(), height.toFloat())
        sh.setFloatUniform("shape1", p.shape1CX, p.shape1CY, p.shape1HW, p.shape1HH)
        sh.setFloatUniform("radii1", p.radius1TL, p.radius1TR, p.radius1BR, p.radius1BL)
        sh.setFloatUniform("shape1L", p.lens1CX, p.lens1CY, p.lens1HW, p.lens1HH)
        sh.setFloatUniform("rimSoft", p.rimSoft)
        sh.setFloatUniform("shape2", p.shape2CX, p.shape2CY, p.shape2HW, p.shape2HH)
        sh.setFloatUniform("radius2", p.radius2)
        sh.setFloatUniform("blendK", p.blendK)
        sh.setFloatUniform("bevel", p.bevel)
        sh.setFloatUniform("refractPx", p.refract)
        sh.setFloatUniform("falloff", p.falloff)
        sh.setFloatUniform("refractDir", if (p.outward) 1f else -1f)
        sh.setFloatUniform("sampleLo", sampleLoX, sampleLoY)
        sh.setFloatUniform("sampleHi", sampleHiX, sampleHiY)
        sh.setFloatUniform("dispersion", p.dispersion)
        sh.setFloatUniform("lightDir", p.lightX, p.lightY)
        sh.setFloatUniform("specStrength", p.spec)
        sh.setFloatUniform("rimBandMax", p.rimBandMax)
        sh.setFloatUniform(
            "tintColor",
            Color.red(p.tint) / 255f,
            Color.green(p.tint) / 255f,
            Color.blue(p.tint) / 255f,
            Color.alpha(p.tint) / 255f
        )
        sh.setFloatUniform("adaptiveTint", if (p.adaptiveTint) 1f else 0f)
        sh.setFloatUniform(
            "glassTint",
            Color.red(p.glassTint) / 255f,
            Color.green(p.glassTint) / 255f,
            Color.blue(p.glassTint) / 255f,
            Color.alpha(p.glassTint) / 255f
        )
        sh.setFloatUniform("dimAmount", p.dim)
        sh.setFloatUniform("satFactor", p.saturation / 100f)
        sh.setFloatUniform("press", p.press)
        sh.setFloatUniform("touchPos", p.touchX, p.touchY)
        sh.setFloatUniform("touchAmp", p.touchAmp)

        val lens = RenderEffect.createRuntimeShaderEffect(sh, "content")
        return if (p.blurRadius > 0.01f) {
            RenderEffect.createChainEffect(
                lens,
                RenderEffect.createBlurEffect(p.blurRadius, p.blurRadius, Shader.TileMode.CLAMP)
            )
        } else {
            lens
        }
    }

    fun release() {
        renderNode.discardDisplayList()
        layerNode.discardDisplayList()
        shader = null
        lastParams = null
    }
}
