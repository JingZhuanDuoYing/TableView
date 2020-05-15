package cn.jingzhuan.tableview.layoutmanager

import android.content.Context
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import cn.jingzhuan.tableview.RowLayout
import cn.jingzhuan.tableview.element.DrawableColumn
import cn.jingzhuan.tableview.element.Row
import cn.jingzhuan.tableview.element.ViewColumn
import java.io.ObjectInputStream
import java.io.Serializable
import kotlin.math.max
import kotlin.math.min

class ColumnsLayoutManager : Serializable {

    internal val specs = TableSpecs(this)

    @Transient
    private var attachedRows = mutableSetOf<RowLayout>()

    private fun readObject(inputStream: ObjectInputStream) {
        inputStream.defaultReadObject()
        attachedRows = mutableSetOf()
    }

    fun updateTableSize(
        columnsSize: Int = this.specs.columnsCount,
        stickyColumns: Int = this.specs.stickyColumnsCount
    ) {
        specs.updateTableSize(columnsSize, stickyColumns)
        attachedRows.forEach { it.row?.forceLayout = true }
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
            dx > 0 -> {
                val maxDx = scrollRange - specs.scrollX
                min(dx, maxDx)
            }
            dx < 0 -> {
                val minDx = -specs.scrollX
                max(dx, minDx)
            }
            else -> 0
        }
        if (consumed == 0 && specs.scrollX <= scrollRange) return 0
        specs.updateScrollX(specs.scrollX + consumed)
        if (specs.scrollX > scrollRange) specs.updateScrollX(scrollRange)
        // 调整当前持有的所有RowLayout
        attachedRows.forEach { it.scrollTo(specs.scrollX, 0) }
        return consumed
    }

    internal fun measureAndLayout(
        context: Context,
        row: Row<*>,
        rowLayout: RowLayout,
        scrollableContainer: ViewGroup
    ) {
        // 如果rowLayout持有scrollableContainer，说明rowLayout之前已经初始化过了，不需要重新创建View对象
        val initialized = rowLayout.indexOfChild(scrollableContainer) >= 0

        if (row.height <= 0) {
            // measure drawable columns first of all
            row.measure(context, specs)
            if (row.height <= 0) {
                row.height = rowLayout.height
            }
        }
        var pendingLayout = false

        // 普通View的Measure/Layout流程
        var viewIndex = 0
        val maxSize = min(row.columns.size, specs.columnsCount)
        for (index in 0 until maxSize) {
            val column = row.columns[index]

            val sticky = index < specs.stickyColumnsCount
            val visible = specs.isColumnVisible(index)

            if (column is DrawableColumn) {
                if (!visible) continue
                // this may happens when columns changed
                if (column.widthWithMargins == 0 || column.heightWithMargins == 0 || specs.visibleColumnsWidth[index] == 0) {
                    // measure drawable column in necessary
                    row.measure(context, specs)
                    if (specs.compareAndSetColumnsWidth(index, column)) {
                        pendingLayout = true
                    }
                }
                continue
            }

            // 对于非ViewColumn，如DrawableColumn等，不需要走如下流程
            if (column !is ViewColumn) continue

            // 未初始化过的，需要新建View对象
            val view =
                if (initialized)
                    rowLayout.getChildAt(viewIndex) ?: column.createView(context)
                else column.createView(context)
            viewIndex++

            view.visibility = if (visible) View.VISIBLE else View.GONE
            if (!visible) {
                column.widthWithMargins = 0
                if (specs.compareAndSetColumnsWidth(index, column)) {
                    pendingLayout = true
                }
                continue
            }

            // 未初始化过，需要将新创建的View添加到ViewGroup
            if (!initialized) {
                if (sticky) {
                    rowLayout.addView(view)
                } else {
                    scrollableContainer.addView(view)
                }
            }

            // 绑定column和view
            column.bindView(view)

            if (column.forceLayout) {
                pendingLayout = true
            }

            if (view.measuredWidth <= 0 || view.measuredHeight <= 0 || column.forceLayout) {
                // 实际Measure
                column.measureView(view)
            }
            column.forceLayout = false

            if (column.heightWithMargins > row.height) {
                row.height = column.heightWithMargins
                pendingLayout = true
            }

            if (specs.compareAndSetColumnsWidth(index, column)) {
                pendingLayout = true
            }

            if (!column.checkLayout(view)) {
                pendingLayout = true
            }
        }

        if (!initialized) {
            // 未初始化过，需要将scrollableContainer添加进rowLayout
            rowLayout.addView(scrollableContainer)
        }

        // 列宽发生变化或者第一次初始化，都需要Layout
        if (pendingLayout || !initialized || row.forceLayout) {
            row.layout(context, specs)
            row.forceLayout = false
            specs.onColumnsWidthChanged()

            viewIndex = 0
            row.columns.forEachIndexed { _, column ->
                if (column !is ViewColumn) return@forEachIndexed
                val currentViewIndex = viewIndex
                viewIndex++
                val view = rowLayout.getChildAt(currentViewIndex) ?: return@forEachIndexed
                column.layoutView(view)
            }
        }

        // scrollableContainer检查是否需要Measure/Layout
        measureAndLayoutScrollableContainer(context, row, rowLayout, scrollableContainer)

        // 校准scrollX
        val scrollRange = specs.computeScrollRange()
        if (specs.scrollX > scrollRange) specs.updateScrollX(scrollRange)
        if (scrollableContainer.scrollX != specs.scrollX) {
            scrollableContainer.scrollTo(specs.scrollX, 0)
        } else {
            scrollableContainer.postInvalidate()
        }
    }

    /**
     * 检查ScrollableContainer是否需要Measure/Layout，并执行
     */
    private fun measureAndLayoutScrollableContainer(
        context: Context,
        row: Row<*>,
        rowLayout: RowLayout,
        scrollableContainer: ViewGroup
    ): Int {
        val scrollableContainerWidth = specs.tableWidth - specs.stickyWidth
        val rowHeight = row.getRowHeight(context)

        // 实际宽度发生变化重新执行Measure
        if (scrollableContainer.width != scrollableContainerWidth || scrollableContainer.height != rowHeight) {
            val widthMeasureSpec =
                MeasureSpec.makeMeasureSpec(max(0, scrollableContainerWidth), MeasureSpec.EXACTLY)
            val heightMeasureSpec = MeasureSpec.makeMeasureSpec(rowHeight, MeasureSpec.EXACTLY)
            scrollableContainer.measure(widthMeasureSpec, heightMeasureSpec)
        }

        // 检查是否需要Layout，并执行
        val left = rowLayout.paddingLeft + specs.stickyWidth
        val right = left + scrollableContainer.measuredWidth
        if (scrollableContainer.left != left || scrollableContainer.top != 0 || scrollableContainer.right != right || scrollableContainer.bottom != rowHeight) {
            scrollableContainer.layout(left, 0, right, rowHeight)
        }
        return scrollableContainer.measuredWidth
    }

}