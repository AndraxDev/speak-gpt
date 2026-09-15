/**
 * Modified and distributed under the MIT License.
 *
 * Original author: QWEA0 (https://github.com/QWEA0)
 * Source: https://github.com/QWEA0/Liquid-Glass-Android/blob/main/liquidglass/src/main/java/com/example/liquidglass/LiquidGlassButton.kt
 *
 * Modified by: AndraxDev (https://github.com/AndraxDev)
 *
 * Modifications include:
 * - Adjusted paddings
 * */

package org.teslasoft.assistant.ui.liquidglass

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
import com.example.liquidglass.LiquidGlassView
import com.example.liquidglass.R

@Suppress("unused")
open class LiquidGlassButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LiquidGlassView(context, attrs, defStyleAttr) {

    /** 内置文字标签（字体/字距等细节直接拿它设置） */
    val textView: TextView = TextView(context)

    /** 显式设置过文字颜色后不再跟随背景明暗自动切换 */
    private var autoTextColor = true

    /** 按钮文字 */
    var text: CharSequence
        get() = textView.text
        set(value) {
            textView.text = value
        }

    init {
        isClickable = true
        isFocusable = true

        textView.textSize = 16f
        textView.typeface = Typeface.DEFAULT_BOLD
        textView.gravity = Gravity.CENTER
        textView.maxLines = 1
        val h = dp(2)
        val v = dp(14)
        textView.setPadding(h, v, h, v)
        addView(textView, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER))
        applyAutoTextColor(isOverLightBackground)

        attrs?.let { parseButtonAttributes(context, it) }
    }

    private fun parseButtonAttributes(context: Context, attrs: AttributeSet) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.LiquidGlassButton)
        try {
            ta.getText(R.styleable.LiquidGlassButton_android_text)?.let { textView.text = it }
            val sizePx = ta.getDimensionPixelSize(R.styleable.LiquidGlassButton_android_textSize, 0)
            if (sizePx > 0) textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, sizePx.toFloat())
            if (ta.hasValue(R.styleable.LiquidGlassButton_android_textColor)) {
                setTextColor(ta.getColor(R.styleable.LiquidGlassButton_android_textColor, Color.WHITE))
            }
        } finally {
            ta.recycle()
        }
    }

    /** 文字字号（sp） */
    fun setTextSize(sp: Float) {
        textView.textSize = sp
    }

    /** 显式指定文字颜色（同时关闭明暗自动切换与文字阴影） */
    fun setTextColor(color: Int) {
        autoTextColor = false
        textView.setTextColor(color)
    }

    override fun onAppearanceChanged(isOverLight: Boolean) {
        if (autoTextColor) applyAutoTextColor(isOverLight)
    }

    /** 暗背景：白字加投影保证可读；亮背景：深色字去投影 */
    private fun applyAutoTextColor(isOverLight: Boolean) {
        if (isOverLight) {
            textView.setTextColor(0xDE000000.toInt())
        } else {
            textView.setTextColor(Color.WHITE)
        }
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
}
