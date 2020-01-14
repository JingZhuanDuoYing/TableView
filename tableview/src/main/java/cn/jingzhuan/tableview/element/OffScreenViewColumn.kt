package cn.jingzhuan.tableview.element

import android.content.Context
import android.graphics.Canvas
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup.MarginLayoutParams

abstract class OffScreenViewColumn : DrawableColumn() {

  protected var view: View? = null
  private var measuredViewWidth = 0
  private var measuredViewHeight = 0

  abstract fun createView(context: Context): View

  override fun prepareToMeasure(context: Context, rowShareElements: RowShareElements) {
    if (null == view) view = createView(context)
  }

  override fun measure(context: Context, rowShareElements: RowShareElements) {
    val view = view ?: return
    val width = width(context)
    val height = height(context)
    val widthMeasureSpec = MeasureSpec.makeMeasureSpec(
        width, if (width < 0) MeasureSpec.UNSPECIFIED else MeasureSpec.EXACTLY
    )
    val heightMeasureSpec = MeasureSpec.makeMeasureSpec(
        height, if (height < 0) MeasureSpec.UNSPECIFIED else MeasureSpec.EXACTLY
    )
    view.measure(widthMeasureSpec, heightMeasureSpec)
    view.layout(0, 0, view.measuredWidth, view.measuredHeight)

    measuredViewWidth = view.measuredWidth
    measuredViewHeight = view.measuredHeight

    val mlp = view.layoutParams as? MarginLayoutParams
    val leftMargin = mlp?.leftMargin ?: 0
    val topMargin = mlp?.topMargin ?: 0
    val rightMargin = mlp?.rightMargin ?: 0
    val bottomMargin = mlp?.bottomMargin ?: 0

    widthWithMargins = leftMargin + measuredViewWidth + rightMargin
    heightWithMargins = topMargin + measuredViewHeight + bottomMargin
  }

  override fun draw(
    context: Context,
    canvas: Canvas,
    rowShareElements: RowShareElements
  ) {
    super.draw(context, canvas, rowShareElements)
    val view = view ?: return

    val mlp = view.layoutParams as? MarginLayoutParams
    val leftMargin = mlp?.leftMargin ?: 0
    val topMargin = mlp?.topMargin ?: 0

    val drawLeft = columnLeft + leftMargin.toFloat()
    val drawTop = columnTop + topMargin.toFloat()
    canvas.save()
    canvas.translate(drawLeft, drawTop)
    view.draw(canvas)
    canvas.restore()
  }

}