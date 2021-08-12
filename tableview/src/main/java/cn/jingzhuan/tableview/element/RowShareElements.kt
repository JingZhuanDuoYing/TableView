package cn.jingzhuan.tableview.element

import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import androidx.annotation.ColorInt
import androidx.annotation.Px
import cn.jingzhuan.tableview.lazyNone

class RowShareElements {

    internal val rect1 by lazyNone { Rect() }
    internal val rect2 by lazyNone { Rect() }

    private var paintLimitCount = 3
    @delegate:Transient
    private val paintPool by lazyNone { mutableMapOf<Int, RowShareTextPaint>() }

    @delegate:Transient
    internal val backgroundPaint by lazyNone {
        Paint().apply {
            isDither = true
            isAntiAlias = true
            style = Paint.Style.FILL
        }
    }

    fun getPaint(@Px textSize: Float, @ColorInt color: Int, typeface: Typeface): RowShareTextPaint {
        val key = getKey(textSize, color, typeface)
        if (paintPool.contains(key)) return paintPool.getValue(key).acquire()
        var paint: RowShareTextPaint? = null
        if (paintPool.size > paintLimitCount) {
            val found = paintPool.entries.find { !it.value.acquired }
            if (null != found) {
                paint = found.value
                paintPool.remove(found.key)
            }
        }
        if (null == paint) paint = RowShareTextPaint()
        paint.isAntiAlias = true
        paint.isDither = true
        paint.color = color
        paint.textSize = textSize
        paint.typeface = typeface
        paintPool[key] = paint
        return paint.acquire()
    }

    private fun getKey(textSize: Float, color: Int, typeface: Typeface): Int {
        var result = 17
        result = result * 31 + textSize.hashCode()
        result = result * 31 + color.hashCode()
        result = result * 31 + typeface.hashCode()
        return result
    }

}