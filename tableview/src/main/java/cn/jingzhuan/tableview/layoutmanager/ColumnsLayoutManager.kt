package cn.jingzhuan.tableview.layoutmanager

import android.content.Context
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import cn.jingzhuan.tableview.RowLayout
import cn.jingzhuan.tableview.dp
import cn.jingzhuan.tableview.element.Column
import cn.jingzhuan.tableview.element.DrawableColumn
import cn.jingzhuan.tableview.element.Row
import cn.jingzhuan.tableview.element.ViewColumn
import cn.jingzhuan.tableview.screenWidth
import java.io.Serializable
import kotlin.math.max
import kotlin.math.min

class ColumnsLayoutManager : Serializable {

    private var columnsSize = 0
    var stickyColumns = 0
        private set

    var stretchMode = false

    @Transient
    var onColumnsWidthWithMarginsChanged: ((ColumnsLayoutManager) -> Unit)? = null

    var columnsWidthWithMargins = IntArray(0)
        private set

    var columnsHeightWithMargins = IntArray(0)
        private set

    var stickyWidthWithMargins = 0
        private set
    var scrollableWidthWithMargins = 0
        private set
    private var scrollableWidth = 0
    private val insetRight = 15F

    var scrollX = 0
        private set

    @Transient
    private val attachedRows = mutableSetOf<RowLayout>()

    fun updateTableSize(
        columnsSize: Int = this.columnsSize,
        stickyColumns: Int = this.stickyColumns
    ) {
        this.columnsSize = columnsSize
        this.stickyColumns = stickyColumns
        onColumnsSizeChanged()
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
        val scrollRange = computeScrollRange()
        val consumed = when {
            dx > 0 -> {
                val maxDx = scrollRange - scrollX
                min(dx, maxDx)
            }
            dx < 0 -> {
                val minDx = -scrollX
                max(dx, minDx)
            }
            else -> 0
        }
        if (consumed == 0 && scrollX <= scrollRange) return 0
        if (scrollX > scrollRange) scrollX = scrollRange
        scrollX += consumed
        // 调整当前持有的所有RowLayout
        attachedRows.forEach { it.scrollTo(scrollX, 0) }
        return consumed
    }

    private fun computeScrollRange(): Int {
        return max(0F, scrollableWidthWithMargins - scrollableWidth + insetRight).toInt()
    }

    /**
     * 用于在非主线程预先 Measure/Layout 内容，减轻实际展示内容时的工作量
     * 对于 [DrawableColumn] 其内容是可直接 Measure/Layout 的
     * 对于 非 [DrawableColumn] 直接使用 column 本身提供的尺寸
     */
    fun measureAndLayoutInBackground(
        context: Context,
        row: Row<*>
    ): Boolean {
        var maxHeight = 0
        var columnsSizeChanged = false
        val maxSize = min(row.columns.size, columnsWidthWithMargins.size)
        for (index in 0 until maxSize) {
            val column = row.columns[index]

            measureColumnInBackground(context, row, column)
            if (compareAndSetColumnsSizeWithMargins(index, column)) columnsSizeChanged = true

            // 记录 maxHeight，由于此函数属于热点代码，节省从 visibleColumnsHeightWithMargins 遍历得到的开销
            maxHeight = max(maxHeight, column.heightWithMargins)
        }

        // 记录 row 自身的测量高度
        val rowHeight = row.height(context)
        if (rowHeight > 0) {
            row.height = rowHeight
        } else {
            val rowMinHeight = row.minHeight(context)
            val height = max(maxHeight, rowMinHeight)
            row.height = height
        }

        // Layout 步骤由 row 自行实现
        if (!stretchMode && row.height > 0) {
            row.layout(context, false, stickyColumns, columnsWidthWithMargins)
        }

        return columnsSizeChanged
    }

    private fun measureColumnInBackground(context: Context, row: Row<*>, column: Column) {
        if (!column.visible()) return

        if (column is DrawableColumn) {
            column.prepareForMeasure(context, row.rowShareElements)
            column.measure(context, row.rowShareElements)
            return
        }

        // 已经有尺寸的，不管是预先Measure得到的还是实际展示时Measure得到的，都不需要重复执行下面代码了
        if (column.widthWithMargins > 0 && column.heightWithMargins > 0) {
            return
        }

        val margins = column.margins(context)

        if (column.widthWithMargins <= 0) {
            val minWidth = column.minWidth(context)
            val width = column.width(context)
            val columnWidth = max(minWidth, width)
            column.widthWithMargins = columnWidth + margins[0] + margins[2]
        }

        if (column.heightWithMargins <= 0) {
            val minHeight = column.minHeight(context)
            val height = column.height(context)
            val columnHeight = max(minHeight, height)
            column.heightWithMargins = columnHeight + margins[1] + margins[3]
        }
    }

    /**
     * 当表整体列宽有变化时调用
     */
    fun onColumnsWidthWithMarginsChanged() {
        // 重新计算固定列总宽度和滑动列总宽度
        stickyWidthWithMargins = 0
        scrollableWidthWithMargins = 0
        for (i in columnsHeightWithMargins.indices) {
            if (i < stickyColumns) {
                stickyWidthWithMargins += columnsWidthWithMargins[i]
            } else {
                scrollableWidthWithMargins += columnsWidthWithMargins[i]
            }
        }
        onColumnsWidthWithMarginsChanged?.invoke(this)
    }

    private fun onColumnsSizeChanged() {
        if (columnsWidthWithMargins.size != columnsSize) {
            columnsWidthWithMargins = enlargeIntArray(columnsWidthWithMargins, columnsSize)
        }
        if (columnsHeightWithMargins.size != columnsSize) {
            columnsHeightWithMargins = enlargeIntArray(columnsHeightWithMargins, columnsSize)
        }
    }

    private fun compareAndSetColumnsSizeWithMargins(index: Int, column: Column): Boolean {
        var columnsSizeChanged = false
        if (columnsWidthWithMargins[index] < column.widthWithMargins) {
            columnsWidthWithMargins[index] = column.widthWithMargins
            columnsSizeChanged = true
        }
        if (columnsHeightWithMargins[index] < column.heightWithMargins) {
            columnsHeightWithMargins[index] = column.heightWithMargins
            columnsSizeChanged = true
        }
        return columnsSizeChanged
    }

    fun measureAndLayoutInForeground(
        context: Context,
        row: Row<*>,
        rowLayout: RowLayout,
        scrollableContainer: ViewGroup,
        skipLayout: Boolean = false
    ) {
        // 如果rowLayout持有scrollableContainer，说明rowLayout之前已经初始化过了，不需要重新创建View对象
        val initialized = rowLayout.indexOfChild(scrollableContainer) >= 0

        if (row.height <= 0) {
            // measure drawable columns first of all
            measureAndLayoutInBackground(context, row)
            if (row.height <= 0) {
                row.height = rowLayout.height
                // stretchMode 不能传 true, 否则会跳过第一次Layout
                row.layout(context, false, stickyColumns, columnsWidthWithMargins)
            }
        }
        var pendingLayout = false

        // 普通View的Measure/Layout流程
        var viewIndex = 0
        val maxSize = min(row.columns.size, columnsWidthWithMargins.size)
        for (index in 0 until maxSize) {
            val column = row.columns[index]

            val sticky = index < stickyColumns

            if (!column.visible()) continue

            if (column is DrawableColumn) {
                // this may happens when columns changed
                if (column.widthWithMargins == 0 || column.heightWithMargins == 0 || columnsWidthWithMargins[index] == 0 || columnsHeightWithMargins[index] == 0) {
                    // measure drawable column in necessary
                    measureColumnInBackground(context, row, column)
                    compareAndSetColumnsSizeWithMargins(index, column)
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

            // 绑定column和view
            column.bindView(view)

            // 未初始化过，需要将新创建的View添加到ViewGroup
            if (!initialized) {
                if (sticky) {
                    rowLayout.addView(view)
                } else {
                    scrollableContainer.addView(view)
                }
            }

            if (column.forceLayout) pendingLayout = true

            if (view.measuredWidth <= 0 || view.measuredHeight <= 0 || column.forceLayout) {
                // 实际Measure
                column.measureView(view)
            }

            // 实际Measure后，如果宽度超过预测量宽度，需要反馈刷新一些保存的状态值
            if (compareAndSetColumnsSizeWithMargins(index, column)) {
                pendingLayout = true
            }

            if (!column.checkLayout(view)) {
                pendingLayout = true
            }
        }

        // 列宽发生变化或者第一次初始化，都需要Layout
        if (!skipLayout && (pendingLayout || !initialized || row.forceLayout)) {
            row.layout(context, stretchMode, stickyColumns, columnsWidthWithMargins)
            onColumnsWidthWithMarginsChanged()

            viewIndex = 0
            row.columns.forEachIndexed { _, column ->
                if (column !is ViewColumn) return@forEachIndexed
                val currentViewIndex = viewIndex
                viewIndex++
                val view = rowLayout.getChildAt(currentViewIndex) ?: return@forEachIndexed
                column.layoutView(view)
            }
            // erase forceLayout after real layout
            row.forceLayout = false
        }

        if (!initialized) {
            // 未初始化过，需要将scrollableContainer添加进rowLayout
            rowLayout.addView(scrollableContainer)
        }

        // scrollableContainer检查是否需要Measure/Layout
        if (!stretchMode) {
            scrollableWidth =
                measureAndLayoutScrollableContainer(context, row, rowLayout, scrollableContainer)
        }

        // 校准scrollX
        val scrollRange = computeScrollRange()
        if (scrollX > scrollRange) scrollX = scrollRange
        if (scrollableContainer.scrollX != scrollX) {
            scrollableContainer.scrollTo(scrollX, 0)
        } else if (!stretchMode) {
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
        // 有时候在RowLayout未测量完毕时就会被调用，这时假设列表宽度是屏幕宽度
        val maxWidth =
            if (rowLayout.width <= 0) context.screenWidth() else rowLayout.width
        val scrollableContainerWidth =
            maxWidth - rowLayout.paddingLeft - rowLayout.paddingRight - stickyWidthWithMargins

        // 实际宽度发生变化重新执行Measure
        if (scrollableContainer.width != scrollableContainerWidth) {
            val widthMeasureSpec =
                MeasureSpec.makeMeasureSpec(max(0, scrollableContainerWidth), MeasureSpec.EXACTLY)
            val heightMeasureSpec = MeasureSpec.makeMeasureSpec(row.height, MeasureSpec.EXACTLY)
            scrollableContainer.measure(widthMeasureSpec, heightMeasureSpec)
        }

        // 检查是否需要Layout，并执行
        val left = rowLayout.paddingLeft + stickyWidthWithMargins
        val right = left + scrollableContainer.measuredWidth
        val bottom = when {
            rowLayout.height > 0 -> rowLayout.height
            row.height(context) > 0 -> row.height(context)
            else -> row.minHeight(context)
        }
        if (scrollableContainer.left != left || scrollableContainer.top != 0 || scrollableContainer.right != right || scrollableContainer.bottom != bottom) {
            scrollableContainer.layout(left, 0, right, bottom)
        }
        return scrollableContainer.measuredWidth
    }

    /**
     * 股票列表在平铺模式下执行，目标是产生不可左右滑动的列表
     * 此表被调用的前提是，rowLayout已经被初始化过，即不需要新创建View对象和添加到rowLayout中
     * 1、按正常逻辑Measure/Layout固定列
     * 2、剩余宽度平均分给剩下的每一列
     * 这样处理可以给调用方一个权重去安排列设置的固定列和非固定列
     */
    fun measureAndLayoutInStretchMode(
        context: Context,
        row: Row<*>,
        rowLayout: RowLayout,
        scrollableContainer: ViewGroup
    ) {
        val columns = row.columns
        if (columns.isEmpty()) return

        val rowHeight = if (rowLayout.height > 0) rowLayout.height else row.height
        val rowWidth =
            if (rowLayout.width > 0) rowLayout.width else context.screenWidth()
        var x = rowLayout.paddingLeft

        // Measure/Layout 固定列
        for (i in 0 until stickyColumns) {
            val column = columns[i]
            if (!column.visible()) continue
            val widthWithMargins = columnsWidthWithMargins.getOrNull(i) ?: continue
            val virtualRight = x + widthWithMargins

            when (column) {
                is ViewColumn -> {
                    val index = columns.filterIsInstance<ViewColumn>()
                        .indexOf(column)

                    rowLayout.getChildAt(index)
                        ?.let {
                            val mlp = it.layoutParams as? MarginLayoutParams
                            val topMargin = mlp?.topMargin ?: 0
                            val rightMargin = mlp?.rightMargin ?: 0
                            val bottomMargin = mlp?.bottomMargin ?: 0

                            val verticalExtra =
                                rowHeight - it.measuredHeight - topMargin - bottomMargin
                            val top = verticalExtra / 2 + topMargin
                            val bottom = top + it.measuredHeight
                            val right = virtualRight - rightMargin
                            val left = right - it.measuredWidth

                            it.layout(left, top, right, bottom)
                        }
                }
                is DrawableColumn -> {
                    val verticalExtra = rowHeight - column.heightWithMargins
                    val top = verticalExtra / 2
                    val bottom = top + column.heightWithMargins
                    val left = virtualRight - column.widthWithMargins
                    column.layout(context, left, top, virtualRight, bottom, row.rowShareElements)
                }
            }
            x += widthWithMargins
        }

        // 按剩余的空间Measure/Layout ScrollableContainer
        val scrollableContainerLeft = x
        val scrollableContainerWidth =
            rowWidth - scrollableContainerLeft - rowLayout.paddingRight
        val widthSpec = MeasureSpec.makeMeasureSpec(scrollableContainerWidth, MeasureSpec.EXACTLY)
        val heightSpec = MeasureSpec.makeMeasureSpec(rowHeight, MeasureSpec.EXACTLY)
        scrollableContainer.measure(widthSpec, heightSpec)
        scrollableContainer.layout(
            scrollableContainerLeft, 0, rowWidth - rowLayout.paddingRight, rowHeight
        )

        // 按剩余的空间计算列宽，Measure/Layout 剩下的列
        val extraRight = context.dp(10F).toInt()
        var scrollableContainerVisibleColumns = 0
        for (i in stickyColumns until columns.size) {
            val column = columns[i]
            if (column.visible()) scrollableContainerVisibleColumns++
        }
        val columnWidth =
            (scrollableContainerWidth - extraRight) / scrollableContainerVisibleColumns
        x = 0
        for (i in stickyColumns until columns.size) {
            val column = columns[i]
            val margins = column.margins(context)
            val virtualRight = x + columnWidth

            when (column) {
                is ViewColumn -> {
                    val index = columns.filterIsInstance<ViewColumn>()
                        .indexOf(column)
                    rowLayout.getChildAt(index)
                        ?.let {
                            val verticalExtra =
                                rowHeight - it.measuredHeight - margins[1] - margins[3]
                            val top = verticalExtra / 2 + margins[1]
                            val bottom = top + it.measuredHeight
                            val right = virtualRight - margins[2]
                            val left = virtualRight - columnWidth + margins[0]
                            it.layout(left, top, right, bottom)
                        }
                }
                is DrawableColumn -> {
                    val verticalExtra =
                        rowHeight - column.heightWithMargins - margins[1] - margins[3]
                    val top = verticalExtra / 2 + margins[1]
                    val bottom = top + column.heightWithMargins
                    val right = virtualRight - margins[2]
                    val left = virtualRight - columnWidth + margins[0]
                    column.layout(context, left, top, right, bottom, row.rowShareElements)
                }
            }

            x += columnWidth
        }
    }

    private fun enlargeIntArray(
        original: IntArray,
        newCapacity: Int
    ): IntArray {
        val newArray = IntArray(newCapacity)
        System.arraycopy(original, 0, newArray, 0, min(original.size, newCapacity))
        return newArray
    }

}