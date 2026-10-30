/**
 * Modified and distributed under the MIT License.
 *
 * Original author: QWEA0 (https://github.com/QWEA0)
 * Source: https://github.com/QWEA0/Liquid-Glass-Android/blob/main/liquidglass/src/main/java/com/example/liquidglass/BackdropCapture.kt
 *
 * Modified by: AndraxDev (https://github.com/AndraxDev)
 *
 * Modifications include:
 * - Experimental version
 * - Adopted to the latest Kotlin version
 * */

package org.teslasoft.assistant.ui.liquidglass

import android.graphics.Canvas
import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.core.graphics.withSave

@RequiresApi(Build.VERSION_CODES.Q)
internal class BackdropCapture {

    private companion object {
        /** 进行中的采样层数（只在主线程改）。> 0 说明当前绘制发生在某块玻璃的采样里 */
        var depth = 0
    }

    // 复用，避免每帧分配
    private val hostLocation = IntArray(2)
    private val childLocation = IntArray(2)
    private val nestedHidden = ArrayList<View>()

    /**
     * 把 [source] 的内容画进 [canvas]，跳过 [glass]
     *
     * @param canvas 画布原点需已对齐 [source] 左上角
     */
    fun draw(canvas: Canvas, source: View, glass: View) {
        drawLevel(canvas, source, glass)
    }

    private fun drawLevel(canvas: Canvas, host: View, glass: View) {
        // 这一层要跳过的直接子级：玻璃本身，或玻璃所在的那条分支。
        // host 不是玻璃的祖先（同级/跨层级/跨 window 的背景来源）时为 null，
        // 这种情况没有重入风险，整棵照常画。
        val branch = childOnPathTo(host, glass)
        drawContent(canvas, host, branch ?: glass)
        if (branch == null || branch === glass) return

        // 分支刚才被跳过了，这里单独补画（坐标取屏幕差，自带滚动/平移）
        host.getLocationOnScreen(hostLocation)
        branch.getLocationOnScreen(childLocation)
        val dx = (childLocation[0] - hostLocation[0]).toFloat()
        val dy = (childLocation[1] - hostLocation[1]).toFloat()
        val clip = (host as? ViewGroup)?.clipChildren != false

        canvas.withSave {
            if (clip) canvas.clipRect(0f, 0f, host.width.toFloat(), host.height.toFloat())
            canvas.translate(dx, dy)
            if (clip) canvas.clipRect(0f, 0f, branch.width.toFloat(), branch.height.toFloat())
            drawLevel(canvas, branch, glass)
        }
    }

    /** 画 [host] 自身，期间把 [hidden] 置为 INVISIBLE 让 dispatchDraw 跳过它 */
    private fun drawContent(canvas: Canvas, host: View, hidden: View) {
        val save = canvas.save()
        // 公开的 View.draw(Canvas) 不带滚动偏移（框架是在 updateDisplayListIfDirty
        // 里补的），这里补上，否则滚动容器作为背景来源时内容会整体错位
        canvas.translate(-host.scrollX.toFloat(), -host.scrollY.toFloat())
        // setTransitionVisibility 只改可见性标志、不触发 invalidate
        hidden.setTransitionVisibility(View.INVISIBLE)
        val nested = depth > 0
        if (nested) hideOtherGlass(host, hidden)
        depth++
        try {
            host.draw(canvas)
        } finally {
            depth--
            if (nested) {
                for (v in nestedHidden) v.setTransitionVisibility(View.VISIBLE)
                nestedHidden.clear()
            }
            hidden.setTransitionVisibility(View.VISIBLE)
            canvas.restoreToCount(save)
        }
    }

    /** 把 [root] 子树里除 [except] 之外的玻璃都临时藏起来（藏掉的整棵子树都不再进入） */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun hideOtherGlass(root: View, except: View) {
        if (root !is ViewGroup) return
        for (i in 0 until root.childCount) {
            val child = root.getChildAt(i)
            if (child === except) continue
            if (child is LiquidGlassView) {
                if (child.isVisible) {
                    child.setTransitionVisibility(View.INVISIBLE)
                    nestedHidden.add(child)
                }
            } else {
                hideOtherGlass(child, except)
            }
        }
    }

    /**
     * [host] 的直接子级里通往 [descendant] 的那一个；
     * [host] 不是 [descendant] 的祖先时返回 null
     */
    private fun childOnPathTo(host: View, descendant: View): View? {
        var child: View = descendant
        var p = descendant.parent
        while (p is View) {
            if (p === host) return child
            child = p
            p = p.parent
        }
        return null
    }
}
