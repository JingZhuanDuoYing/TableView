package cn.jingzhuan.tableview

import android.graphics.*
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import kotlin.math.roundToInt

class TableDecoration(
    private val size: Int,
    private val orientation: Int = AUTO,
    private val startOffset: Int = 0,
    private val endOffset: Int = 0,
    private val skipStartCount: Int = 0,
    private val skipEndCount: Int = 0,
    private val decorationLeftMargin: Int = 0,
    private val decorationTopMargin: Int = 0,
    private val decorationRightMargin: Int = 0,
    private val decorationBottomMargin: Int = 0,
    @ColorInt private val color: Int = Color.TRANSPARENT,
    @ColorInt private val backgroundColor: Int = Color.TRANSPARENT,
    private val drawable: Drawable? = null,
    private val dash: Boolean = false,
    private val dashStrokeWidth: Float = 1F,
    private val dashIntervals: FloatArray = floatArrayOf(10F, 10f),
    private val dashPhase: Float = 8F
) : RecyclerView.ItemDecoration() {

    companion object {
        const val VERTICAL = LinearLayoutManager.VERTICAL
        const val HORIZONTAL = LinearLayoutManager.HORIZONTAL
        const val AUTO = 3
    }

    private val rect by lazyNone { Rect() }
    private val paint by lazyNone {
        Paint().apply {
            isAntiAlias = true
            isDither = true
            color = this@TableDecoration.color
        }
    }
    private val dashPaint by lazyNone {
        Paint().apply {
            isAntiAlias = true
            isDither = true
            color = this@TableDecoration.color
            style = Paint.Style.STROKE
            pathEffect = DashPathEffect(dashIntervals, dashPhase)
            strokeWidth = dashStrokeWidth
        }
    }
    private val path by lazyNone { Path() }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val position = parent.getChildAdapterPosition(view)
        (parent.layoutManager as? GridLayoutManager)?.apply { return }
        val layoutManager = parent.layoutManager as? LinearLayoutManager ?: return
        if (orientation != AUTO && orientation != layoutManager.orientation) return
        val count = state.itemCount
        if (count == 0) return
        val isVertical = layoutManager.orientation == LinearLayoutManager.VERTICAL

        //startOffset
        if (position == 0) {
            if (isVertical) {
                outRect.top += startOffset
            } else {
                outRect.left += startOffset
            }
        }

        //endOffset
        if (position == count - 1) {
            if (isVertical) {
                outRect.bottom += endOffset
            } else {
                outRect.right += endOffset
            }
        }

        //skip
        if (position >= skipStartCount && position < count - skipEndCount) {
            if (isVertical) {
                outRect.bottom += size
            } else {
                outRect.right += size
            }
        }
    }

    override fun onDraw(
        canvas: Canvas,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.onDraw(canvas, parent, state)
        (parent.layoutManager as? GridLayoutManager)?.apply { return }
        val layoutManager = parent.layoutManager as? LinearLayoutManager ?: return
        if (orientation != AUTO && orientation != layoutManager.orientation) return
        val isVertical = layoutManager.orientation == LinearLayoutManager.VERTICAL

        canvas.save()

        var top = parent.paddingTop
        var bottom = parent.height - parent.paddingBottom
        var left = parent.paddingLeft
        var right = parent.width - parent.paddingRight
        if (parent.clipToPadding) {
            canvas.clipRect(left, top, right, bottom)
        }
        val count = state.itemCount

        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            parent.getDecoratedBoundsWithMargins(child, rect)
            val position = parent.getChildAdapterPosition(child)
            if (position < skipStartCount || position >= count - skipEndCount) continue

            if (isVertical) {
                bottom = rect.bottom + child.translationY.roundToInt()
                if (position == count - 1 && endOffset > 0) {
                    bottom -= endOffset
                }
                top = bottom - size
            } else {
                right = rect.right + child.translationX.roundToInt()
                if (position == count - 1 && endOffset > 0) {
                    right -= endOffset
                }
                left = right - size
            }

            rect.set(left, top, right, bottom)
            if (color != backgroundColor) {
                paint.color = backgroundColor
                canvas.drawRect(rect, paint)
            }

            paint.color = color
            if (isVertical) {
                rect.set(left + decorationLeftMargin, top, right - decorationRightMargin, bottom)
            } else {
                rect.set(left, top + decorationTopMargin, right, bottom - decorationBottomMargin)
            }
            when {
                null != drawable -> {
                    drawable.setBounds(rect.left, rect.top, rect.right, rect.bottom)
                    drawable.draw(canvas)
                }
                dash -> {
                    path.reset()
                    val y = rect.centerY()
                        .toFloat()
                    path.moveTo(rect.left.toFloat(), y)
                    path.lineTo(rect.right.toFloat(), y)
                    canvas.drawPath(path, dashPaint)
                }
                else -> canvas.drawRect(rect, paint)
            }
        }

        canvas.restore()
    }

}
