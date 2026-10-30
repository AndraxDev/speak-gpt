/**
 * Modified and distributed under the MIT License.
 *
 * Original author: QWEA0 (https://github.com/QWEA0)
 * Source: https://github.com/QWEA0/Liquid-Glass-Android/blob/main/liquidglass/src/main/java/com/example/liquidglass/GlassRuntimeEffects.kt
 *
 * Modified by: AndraxDev (https://github.com/AndraxDev)
 *
 * Modifications include:
 * - Experimental version
 * - Adopted to the latest Kotlin version
 * */

package org.teslasoft.assistant.ui.liquidglass

import android.graphics.RuntimeColorFilter
import android.graphics.RuntimeXfermode
import android.os.Build
import android.util.Log
import androidx.annotation.ChecksSdkIntAtLeast

@Suppress("unused")
internal object GlassRuntimeEffects {

    private const val TAG = "GlassRuntimeEffects"

    /** 个别设备 AGSL 编译失败时永久回退（同 GlassLensRenderer.shaderBroken 约定） */
    @Volatile
    private var broken = false

    /** 设备是否可用 AGSL 颜色滤镜/混合模式（Android 16 / API 36，且未发生编译失败） */
    @get:ChecksSdkIntAtLeast(api = Build.VERSION_CODES.BAKLAVA)
    val isSupported: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA && !broken

    /**
     * vibrancy 饱和度曲线：
     * - satFactor <= 1 时与线性 ColorMatrix 完全一致（去饱和端不改行为）
     * - satFactor > 1 时按像素当前饱和度衰减提升量（room），并对极亮像素
     *   做高光保护（hl），避免提饱和后溢出发脏
     */
    private const val VIBRANCY_AGSL = """
        uniform half satFactor;

        half4 main(half4 inColor) {
            half a = inColor.a;
            half3 c = inColor.rgb;
            if (a > 0.0001) {
                c = clamp(c / a, 0.0, 1.0);
            }
            half lum = dot(c, half3(0.2126, 0.7152, 0.0722));
            half3 outC;
            if (satFactor <= 1.0) {
                outC = mix(half3(lum), c, satFactor);
            } else {
                half satNow = max(c.r, max(c.g, c.b)) - min(c.r, min(c.g, c.b));
                half room = 1.0 - smoothstep(0.2, 0.85, satNow);
                half hl = 1.0 - smoothstep(0.75, 0.98, lum);
                half amount = 1.0 + (satFactor - 1.0) * mix(0.3, 1.0, room * hl);
                outC = clamp(mix(half3(lum), c, amount), 0.0, 1.0);
            }
            return half4(outC * a, a);
        }
    """

    /**
     * 边缘高光融合混合器：
     * - screen 通道按旧双 pass 的等效权重近似（旧 SCREEN pass 有效透明度约为
     *   OVERLAY pass 的 0.1 倍），overlay 通道全量
     * - 背景亮度 > ~0.72 起白色高光逐渐失效，过渡为轻微压暗描边
     * - dst 近乎透明处退化为普通 src-over（抗锯齿边缘安全）
     */
    private const val RIM_AGSL = """
        half4 main(half4 src, half4 dst) {
            half sa = src.a;
            half da = dst.a;
            if (sa < 0.002) {
                return dst;
            }
            half3 s = src.rgb;
            if (sa > 0.0001) {
                s = clamp(s / sa, 0.0, 1.0);
            }
            half3 d = dst.rgb;
            if (da > 0.0001) {
                d = clamp(d / da, 0.0, 1.0);
            }

            half3 sc = 1.0 - (1.0 - s) * (1.0 - d);
            half3 ov = mix(2.0 * s * d, 1.0 - 2.0 * (1.0 - s) * (1.0 - d), step(0.5, d));

            half3 res = mix(d, sc, sa * 0.1);
            res = mix(res, ov, sa);

            half lum = dot(d, half3(0.2126, 0.7152, 0.0722));
            half bright = smoothstep(0.72, 0.95, lum);
            res = mix(res, d * (1.0 - 0.22 * sa), bright);

            half outA = sa + da * (1.0 - sa);
            half3 blended = mix(s, res, da);
            return half4(blended * outA, outA);
        }
    """

    /**
     * 逐像素自适应染色混合器：src 只提供覆盖率（画不透明白即可），
     * 染色颜色/强度完全由 dst 局部亮度决定，曲线与全局版本一致：
     * smoothstep(0.35, 0.75) → 暗背景白染色 α0.14，亮背景黑染色 α0.22
     */
    private const val ADAPTIVE_TINT_AGSL = """
        half4 main(half4 src, half4 dst) {
            half da = dst.a;
            half3 d = dst.rgb;
            if (da > 0.0001) {
                d = clamp(d / da, 0.0, 1.0);
            }
            half lum = dot(d, half3(0.2126, 0.7152, 0.0722));
            half e = smoothstep(0.35, 0.75, lum);
            half ta = (0.14 + 0.08 * e) * src.a;
            half3 res = mix(d, half3(1.0 - e), ta);
            return half4(res * da, da);
        }
    """

    /**
     * 创建 vibrancy 饱和度滤镜（每个调用方持有并复用自己的实例，
     * 通过 setFloatUniform("satFactor", …) 更新参数后重新挂到 Paint/RenderEffect）
     *
     * @return null = 不支持或编译失败，调用方回退 ColorMatrixColorFilter
     */
    fun createVibrancyFilter(): RuntimeColorFilter? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.BAKLAVA || broken) return null
        return try {
            RuntimeColorFilter(VIBRANCY_AGSL)
        } catch (t: Throwable) {
            markBroken("vibrancy", t)
            null
        }
    }

    /**
     * 创建边缘高光融合混合器（无 uniform，可长期复用）
     *
     * @return null = 不支持或编译失败，调用方回退 SCREEN+OVERLAY 双 pass
     */
    fun createRimBlender(): RuntimeXfermode? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.BAKLAVA || broken) return null
        return try {
            RuntimeXfermode(RIM_AGSL)
        } catch (t: Throwable) {
            markBroken("rim", t)
            null
        }
    }

    /**
     * 创建逐像素自适应染色混合器（无 uniform，可长期复用）
     *
     * @return null = 不支持或编译失败，调用方回退全局染色覆盖层
     */
    fun createAdaptiveTintBlender(): RuntimeXfermode? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.BAKLAVA || broken) return null
        return try {
            RuntimeXfermode(ADAPTIVE_TINT_AGSL)
        } catch (t: Throwable) {
            markBroken("adaptiveTint", t)
            null
        }
    }

    private fun markBroken(which: String, t: Throwable) {
        Log.e(TAG, "AGSL 运行时效果($which)创建失败，永久回退旧路径", t)
        broken = true
    }
}
