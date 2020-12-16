package cn.jingzhuan.tableview.element

import android.content.Context
import android.graphics.Canvas
import android.support.annotation.ColorInt
import android.support.annotation.Px
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import cn.jingzhuan.tableview.RowLayout
import cn.jingzhuan.tableview.annotations.DP
import cn.jingzhuan.tableview.dp
import cn.jingzhuan.tableview.layoutmanager.ColumnsLayoutManager
import cn.jingzhuan.tableview.layoutmanager.TableSpecs
import java.io.ObjectInputStream
import kotlin.math.max
import kotlin.math.min

/**
 * Chenyikang
 * 2018 December 21
 */
abstract class Row<COLUMN : Column>(var columns: List<COLUMN>) :
    IElement {

    @Px
    var rowHeight = 0

    @ColorInt
    var backgroundColor: Int? = null

    /**
     * when forceLayout was true, next measure and layout process must not be ignored
     */
    var forceLayout = true

    @Transient
    internal var rowShareElements = RowShareElements()
        private set

    override var debugUI: Boolean = false

    @DP
    var minWidth: Int = 0

    @DP
    var minHeight: Int = 50

    @DP
    var width: Int = ViewGroup.LayoutParams.MATCH_PARENT

    @DP
    var height: Int = ViewGroup.LayoutParams.WRAP_CONTENT

    /**
     * as an ID for this row
     */
    abstract fun type(): Int

    private fun readObject(inputStream: ObjectInputStream) {
        inputStream.defaultReadObject()
        rowShareElements = RowShareElements()
    }

    @Deprecated("20200806 use variable field instead", ReplaceWith(""))
    @ColorInt
    open fun backgroundColor(context: Context): Int? {
        return null
    }

    @Deprecated("20200806 use variable field instead", ReplaceWith(""))
    open fun minHeight(context: Context): Int {
        return context.dp(minHeight).toInt()
    }

    @Deprecated("20200806 use variable field instead", ReplaceWith(""))
    override fun height(context: Context): Int {
        return ViewGroup.LayoutParams.WRAP_CONTENT
    }

    @Deprecated("20200806 use variable field instead", ReplaceWith(""))
    override fun width(context: Context): Int {
        return ViewGroup.LayoutParams.MATCH_PARENT
    }

    override fun width() = width

    override fun minWidth() = minWidth

    override fun height() = height

    override fun minHeight() = minHeight

    open fun createView(context: Context): ViewGroup {
        val rowLayout = RowLayout(context)
        val widthPx = context.dp(width).toInt()
        val heightPx = context.dp(height).toInt()
        rowLayout.layoutParams = ViewGroup.LayoutParams(widthPx, heightPx)
        rowLayout.minimumHeight = context.dp(minHeight).toInt()
        return rowLayout
    }

    open fun onBindView(view: View, layoutManager: ColumnsLayoutManager) {

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

            if (specs.isColumnVisible(index)) {
                measureColumn(context, column)
            } else {
                column.widthWithMargins = 0
            }
            if (specs.compareAndSetColumnsWidth(index, column)) {
                columnsSizeChanged = true
            }

            // 记录 maxHeight，由于此函数属于热点代码，节省从 visibleColumnsHeightWithMargins 遍历得到的开销
            maxHeight = max(maxHeight, column.heightWithMargins)
        }

        // 记录 row 自身的测量高度
        val rowHeight = context.dp(height).toInt()
        this.rowHeight = if (rowHeight > 0) {
            rowHeight
        } else {
            val rowMinHeight = context.dp(minHeight).toInt()
            max(maxHeight, rowMinHeight)
        }

        return columnsSizeChanged
    }

    internal fun layout(
        context: Context,
        specs: TableSpecs
    ) {
        var x = 0
        val rowHeight = getRowHeight(context)
        val maxSize = min(columns.size, specs.columnsCount)
        for (i in 0 until maxSize) {
            if (i == specs.stickyColumnsCount) x = 0
            val column = columns[i]
            layoutColumn(context, i, column, x, rowHeight, specs)
            x = column.columnRight
        }
    }

    open fun draw(context: Context, canvas: Canvas, stickyWidthWithMargins: Int) {
        if (null != backgroundColor) {
            val paint = rowShareElements.backgroundPaint
            if (paint.color != backgroundColor) paint.color = backgroundColor!!
            canvas.drawRect(0F, 0F, canvas.width.toFloat(), canvas.height.toFloat(), paint)
        }
    }

    internal open fun layoutAndDrawSticky(
        context: Context,
        canvas: Canvas,
        specs: TableSpecs
    ) {
        val rowHeight = getRowHeight(context)
        var x = 0
        for (i in 0 until specs.stickyColumnsCount) {
            if (!specs.isColumnVisible(i)) continue
            val column = columns[i]
            layoutColumn(context, i, column, x, rowHeight, specs)
            if (column is DrawableColumn) column.draw(context, canvas, rowShareElements)
            drawColumnsDivider(canvas, column, specs)
            x = column.columnRight
        }
    }

    internal open fun layoutAndDrawScrollable(
        context: Context,
        canvas: Canvas,
        container: View,
        specs: TableSpecs
    ) {
        val rowHeight = getRowHeight(context)
        val startIndex = specs.scrollableFirstVisibleColumnIndex
        var x = specs.scrollableFirstVisibleColumnLeft

        for (i in startIndex until specs.columnsCount) {
            if (!specs.isColumnVisible(i)) continue
            val column = columns[i]
            layoutColumn(context, i, column, x, rowHeight, specs)
            if (column is DrawableColumn) {
                if (column.columnLeft > container.scrollX + container.width) break
                if (!column.shouldIgnoreDraw(container)) {
                    column.draw(context, canvas, rowShareElements)
                }
            }
            drawColumnsDivider(canvas, column, specs)
            x = column.columnRight
        }
    }

    open fun onClick(
        context: Context,
        rowLayout: View,
        columnView: View? = null,
        column: Column,
        sticky: Boolean,
        x: Int,
        y: Int
    ) {
    }

    open fun onLongClick(
        context: Context,
        rowLayout: View,
        columnView: View? = null,
        column: Column,
        sticky: Boolean,
        x: Int,
        y: Int
    ) {
    }

    internal fun getRowHeight(context: Context): Int {
        return when {
            rowHeight > 0 -> rowHeight
            height > 0 -> context.dp(height).toInt()
            else -> context.dp(minHeight).toInt()
        }
    }

    private fun measureColumn(context: Context, column: Column) {
        if (column is DrawableColumn) {
            column.prepareToMeasure(context, rowShareElements)
            column.measure(context, rowShareElements)
        }

        // 已经有尺寸的，不管是预先Measure得到的还是实际展示时Measure得到的，都不需要重复执行下面代码了
        if (column.widthWithMargins > 0 && column.heightWithMargins > 0) {
            return
        }

        if (column.widthWithMargins <= 0) {
            val minWidthPx = context.dp(minWidth).toInt()
            val widthPx = context.dp(width).toInt()
            val columnWidth = max(minWidthPx, widthPx)
            column.widthWithMargins = columnWidth + column.leftMargin + column.rightMargin
        }

        if (column.heightWithMargins <= 0) {
            val minHeightPx = context.dp(minHeight).toInt()
            val heightPx = context.dp(height).toInt()
            val columnHeight = max(minHeightPx, heightPx)
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
        column.columnRight = x + specs.visibleColumnsWidth[index]
        column.columnBottom = rowHeight

        val top: Int
        val bottom: Int
        if (rowHeight <= 0 && column.heightWithMargins > 0) {
            top = 0
            bottom = column.heightWithMargins
        } else {
            top = (rowHeight - column.heightWithMargins) / 2
            bottom = rowHeight - top
        }

        if (column.widthWithMargins >= specs.visibleColumnsWidth[index] && column.heightWithMargins >= rowHeight) {
            val right = column.columnRight
            val left = right - column.widthWithMargins
            column.layout(context, left, top, right, bottom, rowShareElements)
        } else {
            val spaceRect = rowShareElements.rect1
            spaceRect.set(
                column.columnLeft,
                column.columnTop,
                column.columnRight,
                column.columnBottom
            )
            val columnRect = rowShareElements.rect2
            Gravity.apply(
                column.gravity,
                column.widthWithMargins,
                column.heightWithMargins,
                spaceRect,
                columnRect
            )
            column.layout(
                context,
                columnRect.left,
                columnRect.top,
                columnRect.right,
                columnRect.bottom,
                rowShareElements
            )
        }
        if (column is DrawableColumn) {
            column.prepareToDraw(context, rowShareElements)
        }
    }

    private fun drawColumnsDivider(canvas: Canvas, column: Column, specs: TableSpecs) {
        if (!specs.enableColumnsDivider) return
        canvas.drawLine(
            column.columnRight.toFloat(),
            column.columnTop.toFloat(),
            column.columnRight.toFloat(),
            column.columnBottom.toFloat(),
            specs.columnsDividerPaint
        )
    }

}