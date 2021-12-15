package cn.jingzhuan.tableview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import androidx.core.view.GestureDetectorCompat
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewParent
import android.widget.FrameLayout
import cn.jingzhuan.tableview.element.Row
import cn.jingzhuan.tableview.layoutmanager.ColumnsLayoutManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.max

class RowLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), IRowLayout {

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
    var job: Job? = null

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
        val row = row
        val layoutManager = layoutManager
        if (null == row || null == layoutManager) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            if (layoutManager?.specs?.tableWidth == 0) {
                layoutManager.specs.tableWidth = max(width, measuredWidth)
            }
            return
        }

        val specs = layoutManager.specs
        val totalWidth = if (specs.tableWidth > 0) specs.tableWidth else context.screenWidth()

        val rowHeight = row.getRowHeight(context)
        val resolvedWidth = View.resolveSizeAndState(totalWidth, widthMeasureSpec, 0)
        val resolvedHeight = View.resolveSizeAndState(rowHeight, heightMeasureSpec, 0)
        setMeasuredDimension(resolvedWidth, resolvedHeight)

        // make sure table width was not zero during pre measure process before layout
        if (specs.tableWidth == 0) specs.tableWidth = max(width, measuredWidth)
        if (specs.tableWidth == 0) specs.tableWidth = resources.displayMetrics.widthPixels
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        layoutManager?.attachRowLayout(this)
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
        if (gestureDetector.onTouchEvent(event)) return true
        return super.onTouchEvent(event)
    }

    override fun getChildAt(index: Int): View? {
        // return early if scrollableContainer was not already added in RowLayout yet
        if (super.getChildCount() == 0) return scrollableContainer.getChildAt(index)
        for (i in 0 until super.getChildCount()) {
            val child = super.getChildAt(i) ?: return null
            if (i == index && child != scrollableContainer) return child
            if (child == scrollableContainer) break
            if (i > index) break
        }
        val childCountBeforeScrollableContainer = super.getChildCount() - 1
        for (i in 0 until scrollableContainer.childCount) {
            val child = scrollableContainer.getChildAt(i) ?: return null
            if (i + childCountBeforeScrollableContainer == index) return child
            if (i + childCountBeforeScrollableContainer > index) return null
        }
        return null
    }

    override fun getChildCount() =
        max(0, super.getChildCount() - 1 + scrollableContainer.childCount)

    override fun setScrollX(value: Int) {
        scrollableContainer.scrollX = value
    }

    override fun scrollBy(
        x: Int,
        y: Int
    ) {
        scrollableContainer.scrollBy(x, y)
    }

    override fun scrollTo(
        x: Int,
        y: Int
    ) {
        scrollableContainer.scrollTo(x, y)
    }

    // -----------------------------    public    -----------------------------
    override fun bindRow(row: Row<*>, layoutManager: ColumnsLayoutManager) {
        if (this.row?.type() != row.type()) {
            removeAllViews()
            scrollableContainer.removeAllViews()
        }
        this.row = row
        this.layoutManager = layoutManager
        this.layoutManager?.attachRowLayout(this)

        if (!layoutManager.specs.enableCoroutine || indexOfChild(scrollableContainer) < 0) {
            this.layoutManager?.measureAndLayout(context, row, this, scrollableContainer)
            row.onBindView(this@RowLayout, layoutManager)
            postInvalidate()
        } else {
            job?.cancel()
            job = GlobalScope.launch {
                this@RowLayout.layoutManager?.measureAndLayout(
                    context,
                    row,
                    this@RowLayout,
                    scrollableContainer
                )
                post {
                    job = null
                    row.onBindView(this@RowLayout, layoutManager)
                    postInvalidate()
                }
            }
        }
    }

    fun layout() {
        val row = row ?: return
        layoutManager?.measureAndLayout(context, row, this, scrollableContainer, true)
    }

    // <editor-fold desc="IRowLayout">    ----------------------------------------------------------

    override fun isIndependentScrollRange(): Boolean {
        return false
    }

    override fun onGetParentView(): ViewParent? {
        return parent
    }

    override fun onGetChildAt(index: Int): View? {
        return getChildAt(index)
    }

    override fun onGetRow(): Row<*>? {
        return row
    }

    override fun onScrollTo(x: Int, y: Int) {
        scrollTo(x, y)
    }

    override fun onScrollBy(x: Int) {
        scrollBy(x, 0)
    }

    override fun onGetScrollX(): Int {
        return scrollableContainer.scrollX
    }

    override fun doLayout() {
        layout()
    }

    override fun realChildCount(): Int = super.getChildCount()

    override fun updateScrollX(scrollX: Int) {
        setScrollX(scrollX)
    }

    // </editor-fold desc="IRowLayout">    ---------------------------------------------------------

    // -----------------------------    private    -----------------------------
    private fun onClick(x: Float, y: Float) {
        val row = row ?: return
        val layoutManager = layoutManager ?: return
        val specs = layoutManager.specs
        val index = findColumnIndexByX(x) ?: return
        val isSticky = index < specs.stickyColumnsCount
        val column = row.columns[index]

        val columnView = findViewByCoordinate(isSticky, x, y)
        val relativeX = if (isSticky) {
            x - column.columnLeft
        } else {
            x - scrollableContainer.left + scrollableContainer.scrollX - column.columnLeft
        }
        val relativeY = y - column.columnTop
        row.onRowClick(
            context,
            this@RowLayout,
            columnView,
            column,
            isSticky,
            relativeX.toInt(),
            relativeY.toInt()
        )
    }

    private fun onLongClick(x: Float, y: Float) {
        val row = row ?: return
        val layoutManager = layoutManager ?: return
        val specs = layoutManager.specs
        val index = findColumnIndexByX(x) ?: return
        val isSticky = index < specs.stickyColumnsCount
        val column = row.columns[index]

        val columnView = findViewByCoordinate(isSticky, x, y)
        val relativeX = if (isSticky) {
            x - column.columnLeft
        } else {
            x - scrollableContainer.left + scrollableContainer.scrollX - column.columnLeft
        }
        val relativeY = y - column.columnTop
        row.onRowLongClick(
            context,
            this@RowLayout,
            columnView,
            column,
            isSticky,
            relativeX.toInt(),
            relativeY.toInt()
        )
    }

    private fun findColumnIndexByX(x: Float): Int? {
        val layoutManager = layoutManager ?: return null
        val specs = layoutManager.specs

        if (x < specs.stickyWidth) {
            var stickyColumnLeft = 0
            for (i in 0 until specs.stickyColumnsCount) {
                val columnLeft = stickyColumnLeft
                val columnRight = columnLeft + specs.visibleColumnsWidth[i]
                if (columnLeft <= x && x <= columnRight) {
                    return i
                }
                stickyColumnLeft += specs.visibleColumnsWidth[i]
            }
        }

        if (!specs.isScrollableFirstVisibleMarkValid) {
            specs.resetScrollableFirstVisibleColumn()
        }
        val drawStartIndex = specs.scrollableFirstVisibleColumnIndex
        var scrollableColumnLeft =
            specs.scrollableFirstVisibleColumnLeft - specs.scrollX + specs.stickyWidth
        for (i in drawStartIndex until specs.columnsCount) {
            val columnRight = scrollableColumnLeft + specs.visibleColumnsWidth[i]
            if (scrollableColumnLeft <= x && x <= columnRight) {
                return i
            }
            scrollableColumnLeft += specs.visibleColumnsWidth[i]
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

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            onClick(e.x, e.y)
            return super.onSingleTapUp(e)
        }

        override fun onLongPress(e: MotionEvent) {
            onLongClick(e.x, e.y)
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