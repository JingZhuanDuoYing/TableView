package cn.jingzhuan.tableview.layoutmanager

import android.graphics.Paint
import android.support.annotation.ColorInt
import android.support.v7.widget.RecyclerView
import android.util.SparseIntArray
import cn.jingzhuan.tableview.element.Column
import cn.jingzhuan.tableview.element.HeaderRow
import kotlin.math.max

class TableSpecs(private val layoutManager: ColumnsLayoutManager) {

    var headerRow: HeaderRow<*>? = null

    val columnsWidth = SparseIntArray()

    var stickyColumnsCount = 0
        private set
    var columnsCount = 0
        private set

    var stretchMode = false
    var stretchColumnWidth = 0

    var scrollX = 0
        private set
    /**
     * help to locate column by coordinate
     */
    internal var scrollableFirstVisibleColumnIndex = 0
        private set
    internal var scrollableFirstVisibleColumnLeft = 0
        private set

    var tableWidth = 0
    var stickyWidth = 0
        internal set
    var scrollableVirtualWidth = 0
        internal set

    @ColorInt
    var dividerColor = 0xFF959595.toInt()
        set(value) {
            field = value
            columnsDividerPaint.color = value
        }
    var dividerStrokeWidth = 1

    var enableRowsDivider = false
    var enableColumnsDivider = false
    internal var headerRowsDivider: RecyclerView.ItemDecoration? = null
    internal var mainRowsDivider: RecyclerView.ItemDecoration? = null

    internal val columnsDividerPaint = Paint().apply {
        isDither = true
        isAntiAlias = true
        color = dividerColor
    }

    @Transient
    var onColumnsWidthWithMarginsChanged: ((ColumnsLayoutManager) -> Unit)? = null

    internal fun updateScrollX(scrollX: Int) {
        if (this.scrollX == scrollX) return
        val dx = scrollX - this.scrollX
        this.scrollX = scrollX
        if (scrollableFirstVisibleColumnLeft <= scrollX && scrollableFirstVisibleColumnLeft + columnsWidth[scrollableFirstVisibleColumnIndex] >= scrollX) {
            return
        }
        if (dx > 0) {
            var left =
                scrollableFirstVisibleColumnLeft + columnsWidth[scrollableFirstVisibleColumnIndex]
            for (i in scrollableFirstVisibleColumnIndex + 1 until columnsCount) {
                if (left <= scrollX && left + columnsWidth[i] >= scrollX) {
                    scrollableFirstVisibleColumnIndex = i
                    scrollableFirstVisibleColumnLeft = left
                    return
                }
                left += columnsWidth[i]
            }
        } else {
            var left = scrollableFirstVisibleColumnLeft
            for (i in scrollableFirstVisibleColumnIndex downTo stickyColumnsCount) {
                if (i != scrollableFirstVisibleColumnIndex) left -= columnsWidth[i]
                if (left <= scrollX && left + columnsWidth[i] >= scrollX) {
                    scrollableFirstVisibleColumnIndex = i
                    scrollableFirstVisibleColumnLeft = left
                    return
                }
            }
        }

        // fallback
        resetScrollableFirstVisibleColumn()
    }

    internal fun updateTableSize(columnsCount: Int, stickyColumnsCount: Int) {
        if (stickyColumnsCount > columnsCount) throw IllegalArgumentException("stickyColumnsCount must not be greater than columnsCount")
        this.columnsCount = columnsCount
        this.stickyColumnsCount = stickyColumnsCount
        onColumnsWidthChanged()
        resetScrollableFirstVisibleColumn()
    }

    /**
     * @return whether [columnsWidth] have been changed
     */
    internal fun compareAndSetColumnsWidth(index: Int, column: Column): Boolean {
        var changed = false

        if (stretchMode && index >= stickyColumnsCount) {
            changed = columnsWidth[index] == stretchColumnWidth
            columnsWidth.put(index, stretchColumnWidth)
            return changed
        }

        if (columnsWidth[index] < column.widthWithMargins) {
            columnsWidth.put(index, column.widthWithMargins)
            changed = true
        }

        // force set 0 when column was invisible
        if (!column.visible) {
            columnsWidth.put(index, 0)
            changed = true
        }

        val shouldCalculateStretchColumnWidth = stretchMode && stretchColumnWidth <= 0
        val calculateStretchColumnWidth = (changed or shouldCalculateStretchColumnWidth) && index < stickyColumnsCount
        if (calculateStretchColumnWidth) {
            var stickyWidth = 0
            for (i in 0 until stickyColumnsCount) {
                stickyWidth += columnsWidth[i]
            }
            this.stickyWidth = stickyWidth

            if (stretchMode) {
                var visibleScrollableColumnsCount = 0
                for (i in stickyColumnsCount until columnsCount) {
                    if (isColumnVisible(i)) visibleScrollableColumnsCount++
                }
                if (visibleScrollableColumnsCount > 0) {
                    stretchColumnWidth =
                        (tableWidth - stickyWidth) / visibleScrollableColumnsCount
                }
            }
        }

        if (scrollableFirstVisibleColumnIndex > index) {
            resetScrollableFirstVisibleColumn()
        }

        return changed
    }

    internal fun isColumnVisible(index: Int): Boolean {
        return headerRow?.columns?.getOrNull(index)?.visible ?: true
    }

    fun computeScrollRange(): Int {
        if (stretchMode) return 0
        return max(0, scrollableVirtualWidth - (tableWidth - stickyWidth))
    }

    fun onColumnsWidthChanged() {
        stickyWidth = 0
        scrollableVirtualWidth = 0
        for (i in 0 until columnsCount) {
            if (i < stickyColumnsCount) {
                stickyWidth += columnsWidth[i]
            } else {
                scrollableVirtualWidth += columnsWidth[i]
            }
        }
        onColumnsWidthWithMarginsChanged?.invoke(layoutManager)
    }

    private fun resetScrollableFirstVisibleColumn() {
        var left = 0
        for (i in stickyColumnsCount until columnsCount) {
            if (left <= scrollX && left + columnsWidth[i] >= scrollX) {
                scrollableFirstVisibleColumnIndex = i
                scrollableFirstVisibleColumnLeft = left
                return
            }
            left += columnsWidth[i]
        }
    }

}