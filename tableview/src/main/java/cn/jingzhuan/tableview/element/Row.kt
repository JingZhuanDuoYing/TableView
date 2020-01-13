package cn.jingzhuan.tableview.element

import android.content.Context
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import cn.jingzhuan.tableview.RowLayout
import cn.jingzhuan.tableview.dp
import cn.jingzhuan.tableview.layoutmanager.ColumnsLayoutManager
import kotlin.math.min

/**
 * Chenyikang
 * 2018 December 21
 */
abstract class Row<COLUMN : Column>(var columns: List<COLUMN>) :
    IElement {

    val visibleColumns = columns.filter { it.visible() }
    var height = 0
    var stretchMode = false
    private var isSticky = false
    internal var forceLayout = true

    @Transient
    internal val rowShareElements = RowShareElements()

    fun setSticky(sticky: Boolean) {
        isSticky = sticky
    }

    open fun isSticky() = isSticky

    open fun createView(context: Context): ViewGroup {
        val rowLayout = RowLayout(context)
        rowLayout.layoutParams = ViewGroup.LayoutParams(width(context), height(context))
        rowLayout.minimumHeight = minHeight(context)
        return rowLayout
    }

    open fun bindView(view: ViewGroup, layoutManager: ColumnsLayoutManager) {
        (view as? RowLayout)?.bindRow(this, layoutManager)
    }

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

    fun layout(
        context: Context,
        stretchMode: Boolean,
        stickyColumns: Int,
        columnsWidthWithMargins: IntArray
    ) {
        var x = 0
        val rowHeight = when {
            height > 0 -> height
            height(context) > 0 -> height(context)
            else -> minHeight(context)
        }
        val maxSize = min(visibleColumns.size, columnsWidthWithMargins.size)
        for (i in 0 until maxSize) {
            if (i == stickyColumns) x = 0
            val column = visibleColumns[i]
            if (stretchMode) {
                if (column.laidOut) {
                    column.layout(context, column.left, column.top, column.right, column.bottom, rowShareElements)
                }
                continue
            }

            val columnWidthWithMargins = columnsWidthWithMargins.getOrNull(i) ?: continue

            column.columnLeft = x
            column.columnTop = 0
            column.columnRight = x + columnWidthWithMargins
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

            val right = x + columnWidthWithMargins
            val left = right - column.widthWithMargins
            column.layout(context, left, top, right, bottom, rowShareElements)
            x += columnWidthWithMargins
        }
    }

    open fun draw(context: Context, canvas: Canvas, stickyWidthWithMargins: Int) {

    }

    open fun drawSticky(
        context: Context,
        canvas: Canvas,
        stickyColumns: Int
    ) {
        for (i in 0 until stickyColumns) {
            val column = visibleColumns[i] as? DrawableColumn
                ?: continue
            column.draw(context, canvas, rowShareElements)
        }
    }

    open fun drawScrollable(
        context: Context,
        canvas: Canvas,
        container: View,
        stickyColumns: Int
    ) {
        for (i in stickyColumns until visibleColumns.size) {
            val column = visibleColumns[i] as? DrawableColumn
                ?: continue
            if (column.shouldIgnoreDraw(container)) continue
            column.draw(context, canvas, rowShareElements)
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

}