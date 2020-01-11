package com.nagihong.tableview.element

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Style.STROKE
import android.view.View
import com.nagihong.tableview.drawLine
import com.nagihong.tableview.drawRect
import com.nagihong.tableview.lazyNone

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

  open fun prepareForMeasure(context: Context) {}

  open fun measure(context: Context) {}

  open fun shouldIgnoreDraw(container: View): Boolean {
    val left = container.scrollX
    val right = left + container.width
    val top = container.scrollY
    val bottom = top + container.height
    return this.right < left || this.left > right || this.top > bottom || this.bottom < top
  }

  open fun draw(
    context: Context,
    canvas: Canvas
  ) {
    if (debugUI()) {
      debugPaint.color = Color.RED
      canvas.drawRect(left, top, right, bottom, debugPaint)
      val center = (top + bottom) / 2
      canvas.drawLine(left, center - 1, right, center + 1, debugPaint)

      debugPaint.color = Color.YELLOW
      val margins = margins(context)
      canvas.drawRect(
          left + margins[0],
          top + margins[1],
          right - margins[2],
          bottom - margins[3],
          debugPaint
      )

      debugPaint.color = Color.BLUE
      val padding = padding(context)
      canvas.drawRect(
          left + margins[0] + padding[0],
          top + margins[1] + padding[1],
          right - margins[2] - padding[2],
          bottom - margins[3] - padding[3],
          debugPaint
      )
    }
  }

}