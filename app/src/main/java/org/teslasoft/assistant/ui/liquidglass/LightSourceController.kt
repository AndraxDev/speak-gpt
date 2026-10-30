/**
 * Modified and distributed under the MIT License.
 *
 * Original author: QWEA0 (https://github.com/QWEA0)
 * Source: https://github.com/QWEA0/Liquid-Glass-Android/blob/main/liquidglass/src/main/java/com/example/liquidglass/LightSourceController.kt
 *
 * Modified by: AndraxDev (https://github.com/AndraxDev)
 *
 * Modifications include:
 * - Experimental version
 * - Adopted to the latest Kotlin version
 * */

package org.teslasoft.assistant.ui.liquidglass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import android.view.View
import kotlin.math.abs
import kotlin.math.sqrt

internal object LightSourceController : SensorEventListener {

    /**
     * 默认光源方向（无传感器/设备平放时）：左上方射入，轴向离水平约 30°——沿 iOS 26
     * 控制中心截图里的控件一圈逐角量得，两道亮边的峰值落在 30° / 210°（0° 为右、
     * 顺时针），对应 iOS Liquid Glass 的「左上、右下两道亮边」。
     * 向量是光线的行进方向（view 坐标系 y 向下）
     */
    internal const val DEFAULT_X = 0.866f
    internal const val DEFAULT_Y = 0.5f

    /** 低通滤波系数（SENSOR_DELAY_GAME ≈ 50Hz） */
    private const val SMOOTHING = 0.14f

    /** 通知阈值：方向变化超过该值才回调（避免静止时无意义的重绘） */
    private const val NOTIFY_EPSILON = 0.004f

    // 当前平滑后的光线方向（单位向量，view 坐标系 y 向下）
    @Volatile var lightDirX = DEFAULT_X
        private set
    @Volatile var lightDirY = DEFAULT_Y
        private set

    private val listeners = LinkedHashSet<View>()
    private var sensorManager: SensorManager? = null
    private var registered = false
    private var lastNotifiedX = DEFAULT_X
    private var lastNotifiedY = DEFAULT_Y

    /** 注册一个需要跟随光源重绘的视图（attach 时调用） */
    fun register(view: View) {
        synchronized(listeners) {
            if (!listeners.add(view)) return
            if (!registered) {
                val sm = view.context.applicationContext
                    .getSystemService(Context.SENSOR_SERVICE) as? SensorManager ?: return
                val sensor = sm.getDefaultSensor(Sensor.TYPE_GRAVITY)
                    ?: sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                    ?: return
                sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
                sensorManager = sm
                registered = true
            }
        }
    }

    /** 注销视图（detach 时调用）；最后一个视图注销后停用传感器 */
    fun unregister(view: View) {
        synchronized(listeners) {
            listeners.remove(view)
            if (listeners.isEmpty() && registered) {
                sensorManager?.unregisterListener(this)
                sensorManager = null
                registered = false
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        val gx = event.values[0]
        val gy = event.values[1]

        // 按屏幕方向重映射到"当前朝向"坐标（x 右，y 上）
        val rotation = synchronized(listeners) {
            listeners.firstOrNull()?.display?.rotation ?: Surface.ROTATION_0
        }
        val ux: Float
        val uy: Float
        when (rotation) {
            Surface.ROTATION_90 -> { ux = -gy; uy = gx }
            Surface.ROTATION_180 -> { ux = -gx; uy = -gy }
            Surface.ROTATION_270 -> { ux = gy; uy = -gx }
            else -> { ux = gx; uy = gy }
        }

        // 屏幕平面内的重力分量大小 → 倾斜程度（0 = 平放，1 = 竖直）
        val planar = sqrt(ux * ux + uy * uy)
        val tilt = (planar / SensorManager.GRAVITY_EARTH).coerceIn(0f, 1f)

        // 传感器光线方向：光从"世界上方"来 → dir = -up（y 翻转到 view 坐标系后为 (-ux, uy)）
        var tx = DEFAULT_X
        var ty = DEFAULT_Y
        if (planar > 0.5f) {
            val inv = 1f / planar
            val sx = -ux * inv
            val sy = uy * inv
            // 平放时渐变回默认方向，避免方向抖动
            tx = sx * tilt + DEFAULT_X * (1f - tilt)
            ty = sy * tilt + DEFAULT_Y * (1f - tilt)
        }
        val len = sqrt(tx * tx + ty * ty)
        if (len > 1e-4f) {
            tx /= len
            ty /= len
        }

        // 低通滤波
        var nx = lightDirX + (tx - lightDirX) * SMOOTHING
        var ny = lightDirY + (ty - lightDirY) * SMOOTHING
        val nLen = sqrt(nx * nx + ny * ny)
        if (nLen > 1e-4f) {
            nx /= nLen
            ny /= nLen
        }
        lightDirX = nx
        lightDirY = ny

        // 变化足够大才触发重绘
        if (abs(nx - lastNotifiedX) > NOTIFY_EPSILON || abs(ny - lastNotifiedY) > NOTIFY_EPSILON) {
            lastNotifiedX = nx
            lastNotifiedY = ny
            synchronized(listeners) {
                listeners.forEach { it.postInvalidateOnAnimation() }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
