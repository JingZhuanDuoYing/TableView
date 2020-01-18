package cn.jingzhuan.tableview.element

import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.text.TextPaint

class RowShareElements {

    internal val rect1 = Rect()
    internal val rect2 = Rect()
    @Transient
    private val paintPool = mutableMapOf<Int, TextPaint>()
    @Transient
    internal val backgroundPaint = Paint().apply {
        isDither = true
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    fun getPaint(textSize: Float, color: Int, typeface: Typeface): TextPaint {
        val key = getKey(textSize, color, typeface)
        if (paintPool.contains(key)) return paintPool.getValue(key)
        val paint = TextPaint()
        paint.isAntiAlias = true
        paint.isDither = true
        paint.color = color
        paint.textSize = textSize
        paintPool[key] = paint
        return paint
    }

    private fun getKey(textSize: Float, color: Int, typeface: Typeface): Int {
        var result = 17
        result = result * 31 + textSize.hashCode()
        result = result * 31 + color.hashCode()
        result = result * 31 + typeface.hashCode()
        return result
    }

}