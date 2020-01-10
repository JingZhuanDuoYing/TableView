package com.nagihong.tableview.element

import android.content.Context
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import com.nagihong.tableview.RowLayout
import com.nagihong.tableview.dp
import com.nagihong.tableview.layoutmanager.ColumnsLayoutManager
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

    fun setSticky(sticky: Boolean) {
        isSticky = sticky
    }

    open fun isSticky() = isSticky

    open fun stickyColumnCount(): Int {
        return visibleColumns.count { it.isSticky() }
    }

    open fun createView(context: Context): ViewGroup {
        val rowLayout = RowLayout(context)
        rowLayout.layoutParams = ViewGroup.LayoutParams(width(context), height(context))
        rowLayout.minimumHeight = minHeight(context)
        return rowLayout
    }

    open fun bindView(view: ViewGroup, layoutManager: ColumnsLayoutManager) {
        (view as? RowLayout)?.bindRow(this, stretchMode, layoutManager)
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
        columnsWidthWithMargin: IntArray
    ) {
        var x = 0
        val rowHeight = when {
            height > 0 -> height
            height(context) > 0 -> height(context)
            else -> minHeight(context)
        }
        val stickyCount = stickyColumnCount()
        val maxSize = min(visibleColumns.size, columnsWidthWithMargin.size)
        for (i in 0 until maxSize) {
            if (i == stickyCount) x = 0
            val column = visibleColumns[i]
            if (stretchMode) {
                if (column.laidOut) {
                    column.layout(context, column.left, column.top, column.right, column.bottom)
                }
                continue
            }

            val columnWidthWithMargins = columnsWidthWithMargin.getOrNull(i) ?: continue

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
            column.layout(context, left, top, right, bottom)
            x += columnWidthWithMargins
        }
    }

    fun drawSticky(
        context: Context,
        canvas: Canvas?
    ) {
        canvas ?: return
        val stickyCount = stickyColumnCount()
        for (i in 0 until stickyCount) {
            val column = visibleColumns[i] as? DrawableColumn
                ?: continue
            column.draw(context, canvas)
        }
    }

    fun drawScrollable(
        context: Context,
        canvas: Canvas?,
        container: View
    ) {
        canvas ?: return
        val stickyCount = stickyColumnCount()
        for (i in stickyCount until visibleColumns.size) {
            val column = visibleColumns[i] as? DrawableColumn
                ?: continue
            if (column.shouldIgnoreDraw(container)) continue
            column.draw(context, canvas)
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

    private fun stickyWidth(): Int {
        val stickyColumnCount = stickyColumnCount()
        var width = 0
        for (i in 0 until stickyColumnCount) {
            width += visibleColumns[i].widthWithMargins
        }
        return width
    }

    private fun scrollableWidth(): Int {
        val stickyColumnCount = stickyColumnCount()
        var width = 0
        for (i in stickyColumnCount until visibleColumns.size) {
            width += visibleColumns[i].widthWithMargins
        }
        return width
    }

}