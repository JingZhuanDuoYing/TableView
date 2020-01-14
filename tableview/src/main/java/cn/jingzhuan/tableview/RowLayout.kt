package cn.jingzhuan.tableview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.GestureDetectorCompat
import cn.jingzhuan.tableview.element.Row
import cn.jingzhuan.tableview.layoutmanager.ColumnsLayoutManager

class RowLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val scrollableContainer: FrameLayout by lazyNone {
        HiddenFrameLayout(context)
            .apply {
                layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
                onDrawCallback = { drawScrollableContent(it) }
            }
    }

    init {
        @Suppress("LeakingThis")
        setWillNotDraw(false)
    }

    var row: Row<*>? = null
        private set
    private var layoutManager: ColumnsLayoutManager? = null

    private var lastUpX = 0F
    private var lastUpY = 0F
    private val gestureDetector by lazyNone {
        GestureDetectorCompat(
            context, initGestureListener()
        )
    }

    // -----------------------------    overrides    -----------------------------
    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ) {
        val row = row ?: return super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val layoutManager =
            layoutManager ?: return super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val totalWidth =
            layoutManager.specs.columnsWidth.sum() + paddingLeft + paddingRight
        val rowHeight = when {
            row.height > 0 -> row.height
            row.height(context) > 0 -> row.height(context)
            else -> row.minHeight(context)
        }
        val resolvedWidth = View.resolveSizeAndState(totalWidth, widthMeasureSpec, 0)
        val resolvedHeight = View.resolveSizeAndState(rowHeight, heightMeasureSpec, 0)
        setMeasuredDimension(resolvedWidth, resolvedHeight)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        layoutManager?.attachRowLayout(this)
        if (null != layoutManager) scrollX = layoutManager!!.specs.scrollX
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        layoutManager?.detachRowLayout(this)
    }

    override fun onLayout(
        changed: Boolean,
        l: Int,
        t: Int,
        r: Int,
        b: Int
    ) {
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val row = row ?: return
        val layoutManager = layoutManager ?: return
        row.draw(context, canvas, layoutManager.specs.stickyWidth)
        row.layoutAndDrawSticky(context, canvas, layoutManager.specs)
        scrollableContainer.invalidate()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_UP) {
            lastUpX = event.x
            lastUpY = event.y
        }
        super.onTouchEvent(event)
        return gestureDetector.onTouchEvent(event)
    }

    override fun getChildAt(index: Int): View? {
        val layoutManager = layoutManager ?: return null
        return if (index < layoutManager.specs.stickyColumnsCount) {
            super.getChildAt(index)
        } else {
            scrollableContainer.getChildAt(index - layoutManager.specs.stickyColumnsCount)
        }
    }

    override fun getChildCount() = super.getChildCount() - 1 + scrollableContainer.childCount

    override fun setScrollX(value: Int) {
        scrollableContainer.scrollX = value
    }

    override fun scrollBy(
        x: Int,
        y: Int
    ) = scrollableContainer.scrollBy(x, y)

    override fun scrollTo(
        x: Int,
        y: Int
    ) = scrollableContainer.scrollTo(x, y)

    // -----------------------------    public    -----------------------------
    fun bindRow(row: Row<*>, layoutManager: ColumnsLayoutManager) {
        this.row = row
        this.layoutManager = layoutManager
        this.layoutManager?.attachRowLayout(this)
        this.layoutManager?.measureAndLayoutInForeground(context, row, this, scrollableContainer)
    }

    fun relayoutInStretchMode() {
        val row = row ?: return
        layoutManager?.measureAndLayoutInStretchMode(context, row, this, scrollableContainer)
    }

    // -----------------------------    private    -----------------------------
    private fun onClick(
        x: Float,
        y: Float
    ) {
        val row = row ?: return
        val layoutManager = layoutManager ?: return
        val specs = layoutManager.specs
        val index = findColumnIndexByX(x) ?: return
        val isSticky = index < specs.stickyColumnsCount
        val column = row.columns[index]

        val columnView = findViewByCoordinate(isSticky, x, y)
        if (null == columnView) {
            row.onClick(context, this@RowLayout, column)
        } else {
            val relativeX = if (isSticky) {
                x - columnView.left
            } else {
                x - scrollableContainer.left + scrollableContainer.scrollX - columnView.left
            }
            val relativeY = y - columnView.top
            row.onClick(context, this@RowLayout, column, columnView, relativeX, relativeY)
        }
    }

    private fun onLongClick() {
        val row = row ?: return
        val layoutManager = layoutManager ?: return
        val specs = layoutManager.specs
        val index = findColumnIndexByX(x) ?: return
        val isSticky = index < specs.stickyColumnsCount
        val column = row.columns[index]

        val columnView = findViewByCoordinate(isSticky, x, y)
        if (null == columnView) {
            row.onLongClick(context, this@RowLayout, column)
        } else {
            val relativeX = if (isSticky) {
                x - columnView.left
            } else {
                x - scrollableContainer.left + scrollableContainer.scrollX - columnView.left
            }
            val relativeY = y - columnView.top
            row.onLongClick(context, this@RowLayout, column, columnView, relativeX, relativeY)
        }
    }

    private fun findColumnIndexByX(x: Float): Int? {
        val row = row ?: return null
        val layoutManager = layoutManager ?: return null
        val specs = layoutManager.specs

        if (x < specs.stickyWidth) {
            var stickyColumnLeft = 0
            for (i in 0 until specs.stickyColumnsCount) {
                val columnLeft = stickyColumnLeft
                val columnRight = columnLeft + specs.columnsWidth[i]
                if (lastUpX < columnLeft && lastUpX > columnRight) {
                    return i
                }
                stickyColumnLeft += specs.columnsWidth[i]
            }
        }

        val drawStartIndex = row.findScrollableDrawStartColumnIndex(
            scrollableContainer,
            specs.stickyColumnsCount,
            specs.columnsCount
        )
        var scrollableColumnLeft = 0
        for (i in drawStartIndex until specs.columnsCount) {
            val columnLeft = scrollableColumnLeft + specs.stickyWidth
            val columnRight = columnLeft + specs.columnsWidth[i]
            if (lastUpX < columnLeft && lastUpX > columnRight) {
                return i
            }
            scrollableColumnLeft += specs.columnsWidth[i]
        }

        return null
    }

    private fun findViewByCoordinate(
        sticky: Boolean,
        x: Float,
        y: Float
    ): View? {
        if (sticky) {
            for (i in 0 until childCount) {
                val child = getChildAt(i) ?: continue
                if (child == scrollableContainer) continue
                if (child.left < x && x < child.right) {
                    return child
                }
            }
        } else {
            val relativeX = x - scrollableContainer.left + scrollableContainer.scrollX
            for (i in 0 until scrollableContainer.childCount) {
                val child = scrollableContainer.getChildAt(i) ?: continue
                if (child.left < relativeX && relativeX < child.right) {
                    return child
                }
            }
        }
        return null
    }

    private fun initGestureListener() = object : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            onClick(e.x, e.y)
            return super.onSingleTapConfirmed(e)
        }

        override fun onLongPress(e: MotionEvent?) {
            onLongClick()
            super.onLongPress(e)
        }
    }

    // -----------------------------    private    -----------------------------
    private fun drawScrollableContent(canvas: Canvas) {
        val row = row ?: return
        val layoutManager = layoutManager ?: return
        row.layoutAndDrawScrollable(
            context,
            canvas,
            scrollableContainer,
            layoutManager.specs
        )
    }

}