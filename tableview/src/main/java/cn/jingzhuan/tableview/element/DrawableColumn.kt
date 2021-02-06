package cn.jingzhuan.tableview.element

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Style.STROKE
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import cn.jingzhuan.tableview.dp
import cn.jingzhuan.tableview.drawRect
import cn.jingzhuan.tableview.lazyNone

abstract class DrawableColumn : Column {

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

    constructor() : super()
    constructor(
        minWidth: Int? = null,
        minHeight: Int? = null,
        width: Int? = null,
        height: Int? = null,
        leftMargin: Int? = null,
        topMargin: Int? = null,
        rightMargin: Int? = null,
        bottomMargin: Int? = null,
        paddingLeft: Int? = null,
        paddingTop: Int? = null,
        paddingRight: Int? = null,
        paddingBottom: Int? = null,
        gravity: Int? = null,
        visible: Boolean? = null
    ) : super(
        minWidth,
        minHeight,
        width,
        height,
        leftMargin,
        topMargin,
        rightMargin,
        bottomMargin,
        paddingLeft,
        paddingTop,
        paddingRight,
        paddingBottom,
        gravity,
        visible
    )

    open fun prepareToMeasure(context: Context, rowShareElements: RowShareElements) {}

    open fun measure(context: Context, rowShareElements: RowShareElements) {
        val widthPx = context.dp(width).toInt()
        val minWidthPx = context.dp(minWidth).toInt()
        val isWrapWidth = width == ViewGroup.LayoutParams.WRAP_CONTENT
        widthWithMargins = if (isWrapWidth) {
            leftMargin + paddingLeft + minWidthPx + paddingRight + rightMargin
        } else {
            leftMargin + widthPx + rightMargin
        }

        val heightPx = context.dp(height).toInt()
        val minHeightPx = context.dp(minHeight).toInt()
        val isWrapHeight = height == ViewGroup.LayoutParams.WRAP_CONTENT
        heightWithMargins = if (isWrapHeight) {
            topMargin + paddingTop + minHeightPx + paddingBottom + bottomMargin
        } else {
            topMargin + heightPx + bottomMargin
        }
    }

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
        if (debugUI) {
            debugPaint.color = Color.RED
            canvas.drawRect(columnLeft, columnTop, columnRight, columnBottom, debugPaint)

            debugPaint.color = Color.BLUE
            val container = rowShareElements.rect1
            container.set(columnLeft, columnTop, columnRight, columnBottom)
            val outRect = rowShareElements.rect2
            Gravity.apply(gravity, widthWithMargins, heightWithMargins, container, outRect)
            canvas.drawRect(outRect, debugPaint)
        }
    }

}