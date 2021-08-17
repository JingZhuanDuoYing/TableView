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
        var paint: RowShareTextPaint? = try {
            paintPool[key]
        } catch (e: Exception) {
            // give up handling ConcurrentModificationException, take performance at first position
            e.printStackTrace()
            null
        }
        if (null != paint) {
            paint.acquire()
            paint.doOnRelease = { onReleasePaint(it) }
            return paint
        }
        paint = RowShareTextPaint()
        paint.isAntiAlias = true
        paint.isDither = true
        paint.color = color
        paint.textSize = textSize
        paint.typeface = typeface
        paintPool[key] = paint
        paint.acquire()
        paint.doOnRelease = { onReleasePaint(it) }
        return paint
    }

    private fun getKey(textSize: Float, color: Int, typeface: Typeface): Int {
        var result = 17
        result = result * 31 + textSize.hashCode()
        result = result * 31 + color.hashCode()
        result = result * 31 + typeface.hashCode()
        return result
    }

    private fun onReleasePaint(paint: RowShareTextPaint) {
        if (paintPool.size <= paintLimitCount) return
        val key = getKey(paint.textSize, paint.color, paint.typeface)
        paintPool.remove(key)
    }
}