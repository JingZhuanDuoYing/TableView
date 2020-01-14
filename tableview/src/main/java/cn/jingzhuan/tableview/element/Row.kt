package cn.jingzhuan.tableview.element

import android.content.Context
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import cn.jingzhuan.tableview.RowLayout
import cn.jingzhuan.tableview.dp
import cn.jingzhuan.tableview.layoutmanager.TableSpecs
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

/**
 * Chenyikang
 * 2018 December 21
 */
abstract class Row<COLUMN : Column>(var columns: List<COLUMN>) :
    IElement {

    var height = 0
    internal var forceLayout = true

    @Transient
    internal val rowShareElements = RowShareElements()

    abstract fun type(): Int

    open fun minHeight(context: Context): Int {
        return context.dp(50F).toInt()
    }

    override fun height(context: Context): Int {
        return ViewGroup.LayoutParams.WRAP_CONTENT
    }

    override fun width(context: Context): Int {
        return ViewGroup.LayoutParams.MATCH_PARENT
    }

    open fun createView(context: Context): ViewGroup {
        val rowLayout = RowLayout(context)
        rowLayout.layoutParams = ViewGroup.LayoutParams(width(context), height(context))
        rowLayout.minimumHeight = minHeight(context)
        return rowLayout
    }

    /**
     * 用于在非主线程预先 Measure/Layout 内容，减轻实际展示内容时的工作量
     * 对于 [DrawableColumn] 其内容是可直接 Measure/Layout 的
     * 对于 非 [DrawableColumn] 直接使用 column 本身提供的尺寸
     *
     * @return whether table columns width changed
     */
    fun measure(
        context: Context,
        specs: TableSpecs
    ): Boolean {
        var maxHeight = 0
        var columnsSizeChanged = false
        val maxSize = min(columns.size, specs.columnsCount)
        for (index in 0 until maxSize) {
            val column = columns[index]

            measureColumn(context, column)
            if (specs.compareAndSetColumnsWidth(
                    index,
                    column.widthWithMargins
                )
            ) columnsSizeChanged = true

            // 记录 maxHeight，由于此函数属于热点代码，节省从 visibleColumnsHeightWithMargins 遍历得到的开销
            maxHeight = max(maxHeight, column.heightWithMargins)
        }

        // 记录 row 自身的测量高度
        val rowHeight = height(context)
        height = if (rowHeight > 0) {
            rowHeight
        } else {
            val rowMinHeight = minHeight(context)
            max(maxHeight, rowMinHeight)
        }

        return columnsSizeChanged
    }

    internal fun layout(
        context: Context,
        specs: TableSpecs
    ) {
        if (specs.stretchMode) return
        var x = 0
        val rowHeight = getRowHeight(context)
        val maxSize = min(columns.size, specs.columnsCount)
        for (i in 0 until maxSize) {
            if (i == specs.stickyColumnsCount) x = 0
            val column = columns[i]
            layoutColumn(context, i, column, x, rowHeight, specs)
            x = column.columnRight
        }
        forceLayout = false
    }

    open fun draw(context: Context, canvas: Canvas, stickyWidthWithMargins: Int) {

    }

    internal fun layoutAndDrawSticky(
        context: Context,
        canvas: Canvas,
        specs: TableSpecs
    ) {
        val rowHeight = getRowHeight(context)
        var x = 0
        for (i in 0 until specs.stickyColumnsCount) {
            val column = columns[i]
            layoutColumn(context, i, column, x, rowHeight, specs)
            if (column is DrawableColumn) column.draw(context, canvas, rowShareElements)
            x = column.columnRight
        }
    }

    internal fun layoutAndDrawScrollable(
        context: Context,
        canvas: Canvas,
        container: View,
        specs: TableSpecs
    ) {
        val rowHeight = getRowHeight(context)

        val startIndex =
            findScrollableDrawStartColumnIndex(
                container,
                specs.stickyColumnsCount,
                specs.columnsCount
            )
        var x = specs.getScrollableColumnLeftByIndex(startIndex)

        for (i in startIndex until specs.columnsCount) {
            val column = columns[i]
            layoutColumn(context, i, column, x, rowHeight, specs)
            if (column is DrawableColumn && !column.shouldIgnoreDraw(container)) {
                if (column.columnLeft > container.scrollX + container.width) break
                column.draw(context, canvas, rowShareElements)
            }
            x = column.columnRight
        }
    }

    open fun onClick(
        context: Context,
        rowLayout: View,
        column: Column,
        columnView: View? = null,
        x: Float = 0F,
        y: Float = 0F
    ) {
    }

    open fun onLongClick(
        context: Context,
        rowLayout: View,
        column: Column,
        columnView: View? = null,
        x: Float = 0F,
        y: Float = 0F
    ) {
    }

    internal fun findScrollableDrawStartColumnIndex(container: View, start: Int, end: Int): Int {
        if ((start - end).absoluteValue <= 1) return start
        val center = (start + end) / 2
        val column = columns[center]
        if (column.columnLeft <= container.scrollX && column.columnRight >= container.scrollX) return center
        return if (column.columnLeft > container.scrollX) findScrollableDrawStartColumnIndex(
            container,
            start,
            center
        ) else findScrollableDrawStartColumnIndex(container, center, end)
    }

    private fun getRowHeight(context: Context): Int {
        return when {
            height > 0 -> height
            height(context) > 0 -> height(context)
            else -> minHeight(context)
        }
    }

    private fun measureColumn(context: Context, column: Column) {
        if (!column.visible()) return

        if (column is DrawableColumn) {
            column.prepareToMeasure(context, rowShareElements)
            column.measure(context, rowShareElements)
            column.prepareToDraw(context, rowShareElements)
            return
        }

        // 已经有尺寸的，不管是预先Measure得到的还是实际展示时Measure得到的，都不需要重复执行下面代码了
        if (column.widthWithMargins > 0 && column.heightWithMargins > 0) {
            return
        }

        if (column.widthWithMargins <= 0) {
            val minWidth = column.minWidth(context)
            val width = column.width(context)
            val columnWidth = max(minWidth, width)
            column.widthWithMargins = columnWidth + column.leftMargin + column.rightMargin
        }

        if (column.heightWithMargins <= 0) {
            val minHeight = column.minHeight(context)
            val height = column.height(context)
            val columnHeight = max(minHeight, height)
            column.heightWithMargins = columnHeight + column.topMargin + column.bottomMargin
        }
    }

    private fun layoutColumn(
        context: Context,
        index: Int,
        column: Column,
        x: Int,
        rowHeight: Int,
        specs: TableSpecs
    ) {
        column.columnLeft = x
        column.columnTop = 0
        column.columnRight = x + specs.columnsWidth[index]
        column.columnBottom = height

        val top: Int
        val bottom: Int
        if (rowHeight <= 0 && column.heightWithMargins > 0) {
            top = 0
            bottom = column.heightWithMargins
        } else {
            top = (rowHeight - column.heightWithMargins) / 2
            bottom = rowHeight - top
        }

        val right = column.columnRight
        val left = right - column.widthWithMargins
        column.layout(context, left, top, right, bottom, rowShareElements)
    }

}