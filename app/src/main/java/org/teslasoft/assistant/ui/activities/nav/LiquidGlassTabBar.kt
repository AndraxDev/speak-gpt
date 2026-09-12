/**
 * Modified and distributed under the MIT License.
 *
 * Original author: QWEA0 (https://github.com/QWEA0)
 * Source: https://github.com/QWEA0/Liquid-Glass-Android/blob/main/liquidglass/src/main/java/com/example/liquidglass/LiquidGlassTabBar.kt
 *
 * Modified by: AndraxDev (https://github.com/AndraxDev)
 *
 * Modifications include:
 * - Header removed along with "iOS" references as "iOS" is a trademark of Apple Inc.
 * - Package name
 * - Added property inactiveTintColor so user can override inactive tab tint color
 * */

package org.teslasoft.assistant.ui.activities.nav

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.liquidglass.LiquidGlassView
import com.example.liquidglass.R
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sin

open class LiquidGlassTabBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LiquidGlassView(context, attrs, defStyleAttr) {

    /** 一个标签：标题 + 可选图标（icon 为 null 时该标签退化为纯文字样式） */
    class TabItem(val title: CharSequence, val icon: Drawable? = null)

    /** 标签切换回调（点击、拖拽吸附或代码设置 [selectedIndex] 都会触发） */
    var onTabSelected: ((index: Int) -> Unit)? = null

    /** 选中项着色（默认 null = 跟随背景明暗用白/黑）；可设为品牌色如 0xFF007AFF */
    var selectedTintColor: Int? = null
        set(value) {
            if (field != value) {
                field = value
                updateTabStyles()
            }
        }

    var inactiveTintColor: Int? = null
        set(value) {
            if (field != value) {
                field = value
                updateTabStyles()
            }
        }

    private var selected = 0

    /** 当前选中标签下标；设置时玻璃滴滑过去并触发 [onTabSelected] */
    var selectedIndex: Int
        get() = selected
        set(value) = selectTab(value, animate = true)

    private class TabHolder(val root: LinearLayout, val icon: ImageView?, val label: TextView)

    private val tabsRow = LinearLayout(context)
    private val tabs = mutableListOf<TabHolder>()

    /** 玻璃滴指示：真玻璃，折射标签行的内容（图标滑过时被弯折放大） */
    private val droplet = LiquidGlassView(context)

    private var overLightAppearance = false

    // 触摸状态（整条 bar 统一处理：点击选择 + 拖拽玻璃滴）
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var downX = 0f
    private var dragging = false
    private var settleAnimator: ValueAnimator? = null

    init {
        // 标签/拖拽自己处理，整条 bar 的按压缩放反而突兀
        enablePressEffect = false

        val pad = dp(4)
        setPadding(pad, pad, pad, pad)

        tabsRow.orientation = LinearLayout.HORIZONTAL
        addView(tabsRow, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))

        droplet.apply {
            enablePressEffect = false
            // 玻璃滴内图标保持清晰：只折射不模糊（iOS 的滴是清透的放大镜）
            enableBackdropBlur = false
            cornerRadius = 999f
            // iOS 的滴内部平坦、只在边缘轻微弯折——斜面窄、折射浅，
            // 否则贴近边缘的标签文字会被折射出放大的副本
            bevelWidth = dpF(8)
            refractionHeight = dpF(4)
            dispersionStrength = 0.04f
            visibility = GONE
        }
        // 玻璃滴叠在标签行上方，折射行内容——必须后 add（先绘制行，再绘制滴）。
        // 背景保持默认的直接父容器（= bar 自身）：捕获到的是 bar 已渲染的玻璃面
        // + 图标，滴叠加折射后比 bar 更亮更凸。不能指向 tabsRow——行外区域是
        // 透明的，透镜采到透明会渲染成黑底
        addView(droplet, LayoutParams(0, 0, Gravity.TOP or Gravity.START))

        overLightAppearance = isOverLightBackground
        attrs?.let { parseTabBarAttributes(context, it) }
    }

    private fun parseTabBarAttributes(context: Context, attrs: AttributeSet) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.LiquidGlassTabBar)
        try {
            val entriesId = ta.getResourceId(R.styleable.LiquidGlassTabBar_glassTabEntries, 0)
            if (entriesId != 0) setTabs(resources.getTextArray(entriesId).toList())
        } finally {
            ta.recycle()
        }
    }

    /** 重建标签列表（图标 + 标题），选中项重置为第 0 个（不触发回调） */
    fun setTabs(items: List<TabItem>) {
        tabsRow.removeAllViews()
        tabs.clear()

        items.forEach { item ->
            val root = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
            }
            var icon: ImageView? = null
            val label: TextView
            if (item.icon != null) {
                // iOS 布局：26dp 图标在上，10sp 小字在下
                root.setPadding(dp(2), dp(7), dp(2), dp(7))
                icon = ImageView(context).apply { setImageDrawable(item.icon) }
                root.addView(icon, LinearLayout.LayoutParams(dp(26), dp(26)))
                label = TextView(context).apply {
                    text = item.title
                    textSize = 10f
                    maxLines = 1
                    typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                }
                root.addView(
                    label,
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { topMargin = dp(2) }
                )
            } else {
                // 纯文字标签
                root.setPadding(dp(4), dp(12), dp(4), dp(12))
                label = TextView(context).apply {
                    text = item.title
                    textSize = 14f
                    maxLines = 1
                    typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                }
                root.addView(label)
            }
            tabs += TabHolder(root, icon, label)
            tabsRow.addView(root, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        }

        // 直接改内部状态：初始化不算切换，不触发回调
        selected = 0
        updateTabStyles()
        droplet.visibility = if (tabs.isEmpty()) GONE else VISIBLE
        requestLayout()
    }

    /** 纯文字标签的便捷重载 */
    @JvmName("setTabTitles")
    fun setTabs(titles: List<CharSequence>) = setTabs(titles.map { TabItem(it) })

    // ==================== 选择与样式 ====================

    private fun selectTab(index: Int, animate: Boolean) {
        val clamped = index.coerceIn(0, (tabs.size - 1).coerceAtLeast(0))
        val changed = clamped != selected
        selected = clamped
        if (changed) updateTabStyles()
        if (animate) animateDropletTo(clamped) else syncDroplet()
        if (changed) onTabSelected?.invoke(clamped)
    }

    private fun updateTabStyles() {
        val selectedColor = selectedTintColor
            ?: if (overLightAppearance) 0xE6000000.toInt() else 0xFFFFFFFF.toInt()
        val normalColor = inactiveTintColor ?: if (overLightAppearance) 0x8C000000.toInt() else 0xB8FFFFFF.toInt()

        tabs.forEachIndexed { index, tab ->
            val color = if (index == selected) selectedColor else normalColor
            tab.icon?.imageTintList = ColorStateList.valueOf(color)
            tab.label.setTextColor(color)
        }
        // 玻璃滴折射的是行内容的截图，配色变了要重採一帧
        droplet.invalidate()
    }

    override fun onAppearanceChanged(isOverLight: Boolean) {
        overLightAppearance = isOverLight
        droplet.overLight = isOverLight
        updateTabStyles()
    }

    // ==================== 玻璃滴定位与动画 ====================

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        // 布局完成后标签宽度才可用；动画/拖拽中不要打断当前位置
        if (settleAnimator?.isRunning != true && !dragging) syncDroplet()
    }

    /** 玻璃滴无动画对齐到选中标签（尺寸随标签，位置用 translation 驱动） */
    private fun syncDroplet() {
        val tab = tabs.getOrNull(selected)?.root
        if (tab == null || tab.width <= 0) {
            droplet.visibility = GONE
            return
        }
        droplet.visibility = VISIBLE
        syncDropletSize(tab.width, tab.height)
        droplet.translationX = (tabsRow.left + tab.left - paddingLeft).toFloat()
        droplet.translationY = (tabsRow.top + tab.top - paddingTop).toFloat()
        droplet.scaleX = 1f
        droplet.scaleY = 1f
        droplet.invalidate()
    }

    private fun syncDropletSize(w: Int, h: Int) {
        val lp = droplet.layoutParams as LayoutParams
        if (lp.width != w || lp.height != h) {
            lp.width = w
            lp.height = h
            droplet.layoutParams = lp
        }
    }

    /** 玻璃滴滑到指定标签：过冲弹性 + 距离越远液态拉伸越明显 */
    private fun animateDropletTo(index: Int) {
        val tab = tabs.getOrNull(index)?.root ?: return
        if (tab.width <= 0) return
        syncDropletSize(tab.width, tab.height)

        settleAnimator?.cancel()
        val startX = droplet.translationX
        val targetX = (tabsRow.left + tab.left - paddingLeft).toFloat()
        val targetY = (tabsRow.top + tab.top - paddingTop).toFloat()
        val dist = targetX - startX
        if (abs(dist) < 0.5f) {
            syncDroplet()
            return
        }

        // 液态拉伸幅度：跨的距离越远拉得越长（上限 ~22%），纵向等体积压缩
        val stretch = 0.22f * min(1f, abs(dist) / (tab.width * 3f))
        val overshoot = OvershootInterpolator(1.1f)

        settleAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 380
            addUpdateListener { anim ->
                val t = anim.animatedValue as Float
                val s = sin(PI.toFloat() * min(t * 1.15f, 1f))
                val sx = 1f + stretch * s
                droplet.scaleX = sx
                droplet.scaleY = 1f - stretch * 0.55f * s
                // 过冲/拉伸可能把滴的可视边缘推出 bar——出界区域采不到背景会
                // 渲染成黑，按当前拉伸量钳制在标签行内（到边时像液体抵住壁）
                droplet.translationX =
                    clampDropletX(startX + dist * overshoot.getInterpolation(t), sx)
                droplet.translationY = targetY
                // 折射内容取决于滴与标签行的相对位置，移动中必须逐帧重採
                droplet.invalidate()
            }
            addListener(object : android.animation.AnimatorListenerAdapter() {
                private var canceled = false
                override fun onAnimationCancel(animation: android.animation.Animator) {
                    canceled = true
                }

                override fun onAnimationEnd(animation: android.animation.Animator) {
                    // cancel() 也会走到这里：动画中途重定向时不能落位，
                    // 否则滴会瞬移到新目标、新动画因距离为 0 而不播
                    if (!canceled) syncDroplet()
                }
            })
            start()
        }
    }

    /**
     * 按当前横向拉伸量钳制平移：保证滴的**可视**边缘（含缩放外扩）
     * 始终留在标签行内。拉伸归零时钳制边界收敛回精确落位点
     */
    private fun clampDropletX(x: Float, scaleX: Float): Float {
        val rowW = tabsRow.width.toFloat()
        val w = droplet.width.toFloat()
        if (rowW <= 0f || w <= 0f) return x
        val bulge = (scaleX - 1f) * w / 2f
        val minX = bulge
        val maxX = rowW - w - bulge
        return if (minX <= maxX) x.coerceIn(minX, maxX) else (rowW - w) / 2f
    }

    // ==================== 触摸：点击选择 + 拖拽玻璃滴 ====================

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean = true

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                dragging = false
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (!dragging && abs(event.x - downX) > touchSlop) {
                    dragging = true
                    settleAnimator?.cancel()
                    // 按住拖拽时玻璃滴微微鼓起（iOS 手感）
                    droplet.scaleX = 1.06f
                    droplet.scaleY = 1.06f
                }
                if (dragging) dragDropletTo(event.x)
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (dragging) {
                    dragging = false
                    selectTab(nearestTabIndex(), animate = true)
                } else {
                    selectTab(tabIndexAt(event.x), animate = true)
                }
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                dragging = false
                animateDropletTo(selected)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    /** 玻璃滴跟手：中心对齐手指，按当前鼓起量钳制在标签行范围内 */
    private fun dragDropletTo(x: Float) {
        if (tabsRow.width <= 0 || droplet.width <= 0) return
        val local = x - paddingLeft - droplet.width / 2f
        droplet.translationX = clampDropletX(local, droplet.scaleX)
        droplet.invalidate()
    }

    /** 拖拽松手：按玻璃滴中心找最近的标签 */
    private fun nearestTabIndex(): Int {
        val centerX = droplet.translationX + droplet.width / 2f
        var best = selected
        var bestDist = Float.MAX_VALUE
        tabs.forEachIndexed { index, tab ->
            val tabCenter = tab.root.left + tab.root.width / 2f
            val d = abs(tabCenter - centerX)
            if (d < bestDist) {
                bestDist = d
                best = index
            }
        }
        return best
    }

    /** 点击位置对应的标签（相对 bar 的 x 坐标） */
    private fun tabIndexAt(x: Float): Int {
        val local = x - tabsRow.left
        tabs.forEachIndexed { index, tab ->
            if (local < tab.root.right) return index
        }
        return (tabs.size - 1).coerceAtLeast(0)
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
    private fun dpF(v: Int): Float = v * resources.displayMetrics.density
}