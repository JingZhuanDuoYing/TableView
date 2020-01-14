package cn.jingzhuan.tableview.element

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Style.STROKE
import android.view.View
import cn.jingzhuan.tableview.lazyNone

abstract class DrawableColumn : Column() {

    @delegate:Transient
    private val debugPaint by lazyNone {
        val paint = Paint()
        paint.color = Color.RED
        paint.isDither = true
        paint.isAntiAlias = true
        paint.strokeWidth = 1F
        paint.style = STROKE
        paint
    }

    open fun prepareToMeasure(context: Context, rowShareElements: RowShareElements) {}

    open fun measure(context: Context, rowShareElements: RowShareElements) {}

    open fun shouldIgnoreDraw(container: View): Boolean {
        val left = container.scrollX
        val right = left + container.width
        val top = container.scrollY
        val bottom = top + container.height
        return columnRight < left || columnLeft > right || columnTop > bottom || columnBottom < top
    }

    open fun prepareToDraw(context: Context, rowShareElements: RowShareElements) {}

    open fun draw(
        context: Context,
        canvas: Canvas,
        rowShareElements: RowShareElements
    ) {
        if (debugUI()) {
//            debugPaint.color = Color.RED
//            canvas.drawRect(left, top, right, bottom, debugPaint)
//            val center = (top + bottom) / 2
//            canvas.drawLine(left, center - 1, right, center + 1, debugPaint)
//
//            debugPaint.color = Color.YELLOW
//            canvas.drawRect(
//                left + leftMargin,
//                top + topMargin,
//                right - rightMargin,
//                bottom - bottomMargin,
//                debugPaint
//            )
//
//            debugPaint.color = Color.BLUE
//            canvas.drawRect(
//                left + leftMargin + paddingLeft,
//                top + topMargin + paddingTop,
//                right - rightMargin - paddingRight,
//                bottom - bottomMargin - paddingBottom,
//                debugPaint
//            )
        }
    }

}