package cn.jingzhuan.tableview.layoutmanager

import android.animation.ValueAnimator
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import cn.jingzhuan.tableview.RowLayout
import cn.jingzhuan.tableview.element.DrawableColumn
import cn.jingzhuan.tableview.element.Row
import cn.jingzhuan.tableview.element.ViewColumn
import cn.jingzhuan.tableview.lazyNone
import cn.jingzhuan.tableview.runOnMainThread
import timber.log.Timber
import java.io.ObjectInputStream
import java.io.Serializable
import kotlin.math.max
import kotlin.math.min

class ColumnsLayoutManager : Serializable {

    internal val specs by lazyNone { TableSpecs(this) }

    @Transient
    private var attachedRows = mutableSetOf<RowLayout>()

    @Transient
    private var snapAnimator: ValueAnimator? = null

    private val runnable = Runnable {
        attachedRows.forEach { it.layout() }
    }

    init {
        specs.onColumnsWidthWithMarginsChanged = OnColumnsWidthWithMarginsChanged@{
            val parent = attachedRows.firstOrNull()?.parent as? View
                ?: return@OnColumnsWidthWithMarginsChanged
            parent.removeCallbacks(runnable)
            parent.post(runnable)
        }
    }

    private fun readObject(inputStream: ObjectInputStream) {
        inputStream.defaultReadObject()
        attachedRows = mutableSetOf()
    }

    fun updateTableSize(
        columnsSize: Int = this.specs.columnsCount,
        stickyColumns: Int = this.specs.stickyColumnsCount
    ) {
        updateTableSize(columnsSize, stickyColumns, 0)
    }

    fun updateTableSize(
        columnsSize: Int = this.specs.columnsCount,
        stickyColumns: Int = this.specs.stickyColumnsCount,
        snapColumnsCount: Int = 0
    ) {
        specs.updateTableSize(columnsSize, stickyColumns, snapColumnsCount)
        attachedRows.forEach { it.row?.forceLayout = true }
    }

    fun setCoroutineEnabled(enable: Boolean) {
        specs.enableCoroutine = enable
    }

    fun randomRowLayout(): RowLayout? {
        return attachedRows.firstOrNull()
    }

    fun forceDetachAllRowLayouts() {
        attachedRows.clear()
    }

    fun attachRowLayout(layout: RowLayout) {
        attachedRows.add(layout)
    }

    fun detachRowLayout(layout: RowLayout) {
        attachedRows.remove(layout)
    }

    fun containsRowLayout(layout: RowLayout): Boolean {
        return attachedRows.contains(layout)
    }

    fun scrollHorizontallyBy(dx: Int): Int {
        if (attachedRows.isEmpty()) return 0
        val scrollRange = specs.computeScrollRange()
        val consumed = when {
            specs.scrollX > 0 && dx < 0 && specs.scrollX < -dx -> {
                -specs.scrollX
            }
            specs.scrollX < 0 && dx > 0 && -specs.scrollX < dx -> {
                -specs.scrollX
            }
            dx > 0 -> {
                val maxDx = scrollRange - specs.scrollX
                min(dx, maxDx)
            }
            dx < 0 -> {
                val minDx = -specs.scrollX - specs.getSnapWidth()
                max(dx, minDx)
            }
            else -> 0
        }
        if (consumed == 0 && specs.scrollX <= scrollRange) return 0
        val expectScrollX = specs.scrollX + consumed
        if (expectScrollX > scrollRange) {
            specs.updateScrollX(scrollRange)
        } else {
            specs.updateScrollX(expectScrollX)
        }
        // 调整当前持有的所有RowLayout
        attachedRows.forEach { it.scrollTo(specs.scrollX, 0) }
        return consumed
    }

    /**
     * @return 返回 true 会停止 RecyclerView 的 ViewFlinger，然后可以开始执行 TableView 的 SnapScroll
     */
    internal fun onHorizontalScrollStateChanged(state: Int, dx: Int): Boolean {
        if (specs.snapColumnsCount <= 0) return false
        if (state == RecyclerView.SCROLL_STATE_SETTLING && specs.scrollX < 0) {
            val snapWidth = specs.getSnapWidth()
            if (snapWidth <= 0) return false
            snapAnimator?.cancel()
            val endX = if (dx > 0) 0 else -snapWidth
            val animator = ValueAnimator.ofInt(specs.scrollX, endX)
            animator.interpolator = AccelerateDecelerateInterpolator()
            animator.addUpdateListener {
                val x = it.animatedValue as? Int ?: return@addUpdateListener
                val animateDx = x - specs.scrollX
                scrollHorizontallyBy(animateDx)
            }
            snapAnimator = animator
            snapAnimator?.start()
            return true
        } else if (state == RecyclerView.SCROLL_STATE_IDLE && specs.scrollX < 0) {
            if (snapAnimator?.isRunning == true) return true
            val snapWidth = specs.getSnapWidth()
            if (snapWidth <= 0) return false
            snapAnimator?.cancel()
            val endX = if (snapWidth + specs.scrollX > -specs.scrollX) 0 else -snapWidth
            val animator = ValueAnimator.ofInt(specs.scrollX, endX)
            animator.interpolator = AccelerateDecelerateInterpolator()
            animator.addUpdateListener {
                val x = it.animatedValue as? Int ?: return@addUpdateListener
                val animateDx = x - specs.scrollX
                scrollHorizontallyBy(animateDx)
            }
            snapAnimator = animator
            snapAnimator?.start()
        }
        return false
    }

    internal fun adjustSnapScrollXAfterColumnsWidthChanged() {
        if (specs.scrollX >= 0) return
        val snapWidth = specs.getSnapWidth()
        if (snapWidth <= 0) return
        val endX = if (snapWidth + specs.scrollX > -specs.scrollX) 0 else -snapWidth
        if (specs.scrollX == endX) return
        val dx = endX - specs.scrollX
        scrollHorizontallyBy(dx)
    }

    internal fun measureAndLayout(
        context: Context,
        row: Row<*>,
        rowLayout: RowLayout,
        scrollableContainer: ViewGroup,
        layoutOnly: Boolean = false
    ) {
        // 如果rowLayout持有scrollableContainer，说明rowLayout之前已经初始化过了，不需要重新创建View对象
        val initialized = rowLayout.indexOfChild(scrollableContainer) >= 0

        var pendingLayout = false

        if (row.rowHeight <= 0 && !layoutOnly) {
            // measure drawable columns first of all
            if (row.measure(context, specs)) pendingLayout = true
            if (row.rowHeight <= 0) {
                row.rowHeight = rowLayout.height
            }
        }

        // 普通View的Measure/Layout流程
        val maxSize = min(row.columns.size, specs.columnsCount)
        if (!layoutOnly) {
            var scrollableViewIndex = -1
            var stickyViewIndex = -1
            for (index in 0 until maxSize) {
                val column = row.columns[index]

                val sticky = index < specs.stickyColumnsCount
                val snap = (index - specs.stickyColumnsCount) < specs.snapColumnsCount
                val visible = specs.isColumnVisible(index)

                if (column is DrawableColumn) {
                    if (!visible) continue
                    // this may happens when columns changed
                    val heightNotDetermined =
                        column.heightWithMargins == 0 && column.height != ViewGroup.LayoutParams.MATCH_PARENT
                    if (row.forceLayout || column.widthWithMargins == 0 || heightNotDetermined || specs.visibleColumnsWidth[index] == 0) {
                        // measure drawable column in necessary
                        if (row.measure(context, specs)) pendingLayout = true
                    }
                    continue
                }

                // 对于非ViewColumn，如DrawableColumn等，不需要走如下流程
                if (column !is ViewColumn) continue

                if (sticky) ++stickyViewIndex else ++scrollableViewIndex

                // 未初始化过的，需要新建View对象
                val view =
                    if (initialized) {
                        (if (sticky) rowLayout.getChildAt(stickyViewIndex)
                            ?: column.createView(context)
                        else scrollableContainer.getChildAt(scrollableViewIndex))
                            ?: column.createView(context)
                    } else {
                        column.createView(context)
                    }

                if (!initialized) {
                    // 未初始化过，需要将新创建的View添加到ViewGroup
                    rowLayout.runOnMainThread {
                        if (sticky) {
                            rowLayout.addView(view)
                        } else {
                            scrollableContainer.addView(view)
                        }
                    }
                } else {
                    val addView = (sticky && rowLayout.indexOfChild(view) < 0)
                            || (!sticky && scrollableContainer.indexOfChild(view) < 0)
                    if (addView) {
                        rowLayout.runOnMainThread {
                            if (sticky) rowLayout.addView(
                                view,
                                (rowLayout.childCount - 2).coerceAtLeast(0)
                            )
                            else scrollableContainer.addView(view)
                        }
                        pendingLayout = true
                    }
                }

                val visibilityChanged = (visible && view.visibility != View.VISIBLE)
                        || (!visible && view.visibility != View.GONE)
                if (visibilityChanged) {
                    pendingLayout = true
                }

                view.visibility = if (visible) View.VISIBLE else View.GONE
                if (!visible) {
                    column.widthWithMargins = 0
                    if (specs.compareAndSetColumnsWidth(index, column)) {
                        pendingLayout = true
                    }
                    continue
                }

                // 绑定column和view
                rowLayout.runOnMainThread {
                    column.bindView(view, row)
                }

                if (view.measuredWidth <= 0 || view.measuredHeight <= 0 || row.forceLayout || column.forceLayout || visibilityChanged) {
                    // 实际Measure
                    column.measureView(view)
                }

                if (column.forceLayout) {
                    pendingLayout = true
                }

                if (column.heightWithMargins > row.rowHeight) {
                    row.rowHeight = column.heightWithMargins
                    pendingLayout = true
                }

                if (specs.compareAndSetColumnsWidth(index, column)) {
                    pendingLayout = true
                }

                if (!column.checkLayout(view)) {
                    pendingLayout = true
                }
            }

            if (initialized) {
                val prePendingRemoveStickyCount = rowLayout.realChildCount() - 2 - stickyViewIndex
                val prePendingRemoveScrollableCount =
                    scrollableContainer.childCount - 1 - scrollableViewIndex
                if (prePendingRemoveStickyCount > 0 || prePendingRemoveScrollableCount > 0) {
                    rowLayout.runOnMainThread {
                        val pendingRemoveStickyCount =
                            rowLayout.realChildCount() - 2 - stickyViewIndex
                        val pendingRemoveScrollableCount =
                            scrollableContainer.childCount - 1 - scrollableViewIndex
                        for (i in 0 until pendingRemoveStickyCount) {
                            rowLayout.removeViewAt(rowLayout.realChildCount() - 2)
                        }
                        for (i in 0 until pendingRemoveScrollableCount) {
                            scrollableContainer.removeViewAt(scrollableContainer.childCount - 1)
                        }
                    }
                }
            }
        }

        if (!initialized) {
            // 未初始化过，需要将scrollableContainer添加进rowLayout
            rowLayout.runOnMainThread {
                if (scrollableContainer.parent == null) rowLayout.addView(scrollableContainer)
            }
        }

        // 列宽发生变化或者第一次初始化，都需要Layout
        if (layoutOnly || pendingLayout || !initialized || row.forceLayout) {
            if (specs.stretchMode) specs.compareAndSetStretchColumnsWidth()
            if (specs.compareAndSetSnapColumnsWidth()) measureSnapViewColumns()
            row.layout(context, specs)

            var viewIndex = 0
            row.columns.forEachIndexed { _, column ->
                if (column !is ViewColumn) return@forEachIndexed
                val currentViewIndex = viewIndex
                viewIndex++
                val view = rowLayout.getChildAt(currentViewIndex) ?: return@forEachIndexed
                column.layoutView(view, column.forceLayout)
                column.forceLayout = false
            }

            if (!row.forceLayoutLock) row.forceLayout = false
            // layoutOnly 模式不调用，因为并没有执行任何实际的 measure，不能确定列宽是否变化
            if (pendingLayout) specs.onColumnsWidthChanged()
        }

        // scrollableContainer检查是否需要Measure/Layout
        measureAndLayoutScrollableContainer(
            context,
            row,
            rowLayout,
            scrollableContainer,
            layoutOnly
        )

        // 校准scrollX
        val scrollRange = specs.computeScrollRange()
        if (specs.scrollX > scrollRange) specs.updateScrollX(scrollRange)
        if (scrollableContainer.scrollX != specs.scrollX) {
            scrollableContainer.scrollTo(specs.scrollX, 0)
        } else {
            scrollableContainer.postInvalidate()
        }

        if (pendingLayout) adjustSnapScrollXAfterColumnsWidthChanged()
    }

    /**
     * 检查ScrollableContainer是否需要Measure/Layout，并执行
     */
    private fun measureAndLayoutScrollableContainer(
        context: Context,
        row: Row<*>,
        rowLayout: RowLayout,
        scrollableContainer: ViewGroup,
        layoutOnly: Boolean = false
    ): Int {
        val scrollableContainerWidth = specs.tableWidth - specs.stickyWidth
        val rowHeight = row.getRowHeight(context)

        // 实际宽度发生变化重新执行Measure
        if (!layoutOnly) {
            if (scrollableContainer.width != scrollableContainerWidth || scrollableContainer.height != rowHeight) {
                val widthMeasureSpec =
                    MeasureSpec.makeMeasureSpec(
                        max(0, scrollableContainerWidth),
                        MeasureSpec.EXACTLY
                    )
                val heightMeasureSpec = MeasureSpec.makeMeasureSpec(rowHeight, MeasureSpec.EXACTLY)
                scrollableContainer.measure(widthMeasureSpec, heightMeasureSpec)
            }
        }

        // 检查是否需要Layout，并执行
        val left = rowLayout.paddingLeft + specs.stickyWidth
        val right = left + scrollableContainer.measuredWidth
        if (layoutOnly || scrollableContainer.left != left || scrollableContainer.top != 0 || scrollableContainer.right != right || scrollableContainer.bottom != rowHeight) {
            scrollableContainer.layout(left, 0, right, rowHeight)
        }
        return scrollableContainer.measuredWidth
    }

    private fun measureSnapViewColumns() {
        if (specs.snapColumnsCount <= 0) return
        attachedRows.forEach {
            var viewIndex = 0
            @Suppress("UseWithIndex")
            for (i in 0 until specs.stickyColumnsCount + specs.snapColumnsCount) {
                val column = it.row?.columns?.getOrNull(i) ?: return@forEach
                if (column !is ViewColumn) continue
                val currentViewIndex = viewIndex
                viewIndex++
                if (i < specs.stickyColumnsCount) continue
                val view = it.getChildAt(currentViewIndex) ?: return@forEach
                view.layoutParams.width = specs.visibleColumnsWidth[i]
                column.measureView(view)
            }
        }
    }

}