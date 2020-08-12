package cn.jingzhuan.tableview.element

import android.content.Context
import android.graphics.Canvas
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup.MarginLayoutParams
import cn.jingzhuan.tableview.dp

abstract class OffScreenViewColumn : DrawableColumn {

  protected var view: View? = null
  private var measuredViewWidth = 0
  private var measuredViewHeight = 0

  constructor() : super()
  constructor(
    minWidth: Int?,
    minHeight: Int?,
    width: Int?,
    height: Int?,
    leftMargin: Int?,
    topMargin: Int?,
    rightMargin: Int?,
    bottomMargin: Int?,
    paddingLeft: Int?,
    paddingTop: Int?,
    paddingRight: Int?,
    paddingBottom: Int?,
    gravity: Int?,
    visible: Boolean?
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

  abstract fun createView(context: Context): View

  override fun prepareToMeasure(context: Context, rowShareElements: RowShareElements) {
    if (null == view) view = createView(context)
  }

  override fun measure(context: Context, rowShareElements: RowShareElements) {
    val view = view ?: return
    val widthPx = context.dp(width).toInt()
    val heightPx = context.dp(height).toInt()
    val widthMeasureSpec = MeasureSpec.makeMeasureSpec(
        widthPx, if (widthPx < 0) MeasureSpec.UNSPECIFIED else MeasureSpec.EXACTLY
    )
    val heightMeasureSpec = MeasureSpec.makeMeasureSpec(
        heightPx, if (heightPx < 0) MeasureSpec.UNSPECIFIED else MeasureSpec.EXACTLY
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