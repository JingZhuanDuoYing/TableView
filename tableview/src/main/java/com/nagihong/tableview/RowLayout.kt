package com.nagihong.tableview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.GestureDetectorCompat
import com.nagihong.tableview.element.Column
import com.nagihong.tableview.element.Row
import com.nagihong.tableview.layoutmanager.ColumnsLayoutManager

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

    private val extraRight by lazyNone { context.dp(10F).toInt() }

    private var row: Row<*>? = null
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
        val stateLayoutManager =
            layoutManager ?: return super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val totalWidth =
            stateLayoutManager.visibleColumnsWidthWithMargins.sum() + paddingLeft + paddingRight
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
        if (null != layoutManager) scrollX = layoutManager!!.scrollX
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

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        row?.drawSticky(context, canvas)
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
        val stickyCount = row?.stickyColumnCount() ?: return null
        return if (index < stickyCount) {
            super.getChildAt(index)
        } else {
            scrollableContainer.getChildAt(index - stickyCount)
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
    fun bindRow(
        row: Row<*>,
        stretchMode: Boolean,
        layoutManager: ColumnsLayoutManager
    ) {
        this.row = row
        this.layoutManager = layoutManager
        this.layoutManager?.attachRowLayout(this)
        this.layoutManager?.measureAndLayoutInForeground(
            context, row, this, scrollableContainer, stretchMode = stretchMode
        )
    }

    fun relayoutInStretchMode() {
        val row = row ?: return
        layoutManager?.measureAndLayoutInStretchMode(context, row, this, scrollableContainer)
    }

    fun computeScrollRange(): Int {
        val rowScrollableWidth =
            layoutManager?.scrollableWidthWithMargins ?: scrollableContainer.width
        return rowScrollableWidth - scrollableContainer.width + extraRight
    }

    fun computeScrollWidth() = scrollableContainer.width

    // -----------------------------    private    -----------------------------
    private fun onClick(
        x: Float,
        y: Float
    ) {
        val row = row ?: return
        var found: Column? = null

        for (column in row.visibleColumns) {
            val offset = if (column.isSticky()) 0 else {
                scrollableContainer.left - scrollableContainer.scrollX
            }
            val start = column.left + offset
            val end = column.right + offset
            if (lastUpX < end && lastUpX > start) {
                found = column
                break
            }
        }

        if (null == found) {
            val columnsSize = layoutManager?.visibleColumnsWidthWithMargins
            if (null != columnsSize) {
                var last = 0
                for (i in columnsSize.indices) {
                    val column = row.visibleColumns.getOrNull(i) ?: continue
                    val offset = if (column.isSticky()) 0 else {
                        scrollableContainer.left - scrollableContainer.scrollX
                    }
                    val start = last + offset
                    val end = last + columnsSize[i] + offset
                    if (lastUpX < end && lastUpX > start) {
                        found = column
                        break
                    }
                    last = end
                }
            }
        }

        found?.apply {
            val columnView = findViewByCoordinate(isSticky(), x, y)
            if (null == columnView) {
                row.onClick(context, this@RowLayout, this)
            } else {
                val relativeX = if (isSticky()) {
                    x - columnView.left
                } else {
                    x - scrollableContainer.left + scrollableContainer.scrollX - columnView.left
                }
                val relativeY = y - columnView.top
                row.onClick(context, this@RowLayout, this, columnView, relativeX, relativeY)
            }
        }
    }

    private fun onLongClick() {
        val row = row ?: return
        var found: Column? = null
        row.visibleColumns.forEach { column ->
            val offset = if (column.isSticky()) 0 else {
                scrollableContainer.left - scrollableContainer.scrollX
            }
            val thisStart = column.left + offset
            val nextStart = column.left + offset
            if (!column.isSticky()) {
                if (nextStart < scrollableContainer.left) return@forEach
                if (thisStart > scrollableContainer.right) {
                    found = column
                    return@forEach
                }
            }
            if (lastUpX >= thisStart && lastUpX <= nextStart) {
                found = column
                return@forEach
            }
        }

        if (null == found) {
            val columnsSize = layoutManager?.visibleColumnsWidthWithMargins
            if (null != columnsSize) {
                var last = 0
                for (i in columnsSize.indices) {
                    if (i >= row.visibleColumns.size) break
                    val column = row.visibleColumns.getOrNull(i) ?: continue
                    val offset = if (column.isSticky()) 0 else {
                        scrollableContainer.left - scrollableContainer.scrollX
                    }
                    val start = last + offset
                    val end = last + columnsSize[i] + offset
                    if (lastUpX < end && lastUpX > start) {
                        found = column
                        break
                    }
                    last = end
                }
            }
        }

        found?.apply {
            val columnView = findViewByCoordinate(isSticky(), x, y)
            if (null == columnView) {
                row.onLongClick(context, this@RowLayout, this)
            } else {
                val relativeX = if (isSticky()) {
                    x - columnView.left
                } else {
                    x - scrollableContainer.left + scrollableContainer.scrollX - columnView.left
                }
                val relativeY = y - columnView.top
                row.onLongClick(context, this@RowLayout, this, columnView, relativeX, relativeY)
            }
        }
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
    fun drawScrollableContent(canvas: Canvas) {
        row?.drawScrollable(context, canvas, scrollableContainer)
    }

}