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

    val visibleColumnsWidth = SparseIntArray()
    private val realColumnsWidth = SparseIntArray()

    var stickyColumnsCount = 0
        private set
    var columnsCount = 0
        private set

    var stretchMode = false
    private var averageStretchColumnWidth = 0

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
    var visibleScrollableVirtualWidth = 0
        internal set
    var realScrollableVirtualWidth = 0
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
        if (scrollableFirstVisibleColumnLeft <= scrollX && scrollableFirstVisibleColumnLeft + visibleColumnsWidth[scrollableFirstVisibleColumnIndex] >= scrollX) {
            return
        }
        if (dx > 0) {
            var left =
                scrollableFirstVisibleColumnLeft + visibleColumnsWidth[scrollableFirstVisibleColumnIndex]
            for (i in scrollableFirstVisibleColumnIndex + 1 until columnsCount) {
                if (left <= scrollX && left + visibleColumnsWidth[i] >= scrollX) {
                    scrollableFirstVisibleColumnIndex = i
                    scrollableFirstVisibleColumnLeft = left
                    return
                }
                left += visibleColumnsWidth[i]
            }
        } else {
            var left = scrollableFirstVisibleColumnLeft
            for (i in scrollableFirstVisibleColumnIndex downTo stickyColumnsCount) {
                if (i != scrollableFirstVisibleColumnIndex) left -= visibleColumnsWidth[i]
                if (left <= scrollX && left + visibleColumnsWidth[i] >= scrollX) {
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
     * @return whether [visibleColumnsWidth] have been changed
     */
    internal fun compareAndSetColumnsWidth(index: Int, column: Column): Boolean {
        var changed = false

        if (realColumnsWidth[index] < column.widthWithMargins) {
            val expand = column.widthWithMargins - realColumnsWidth[index]
            val newRealScrollableVirtualWidth = realScrollableVirtualWidth + expand
            if(stretchMode && realColumnsWidth[index] > 0) {
                for(i in stickyColumnsCount until columnsCount) {
                    val weight = realColumnsWidth[i] / realScrollableVirtualWidth.toFloat()
                    val newWidth = (newRealScrollableVirtualWidth * weight).toInt()
                    visibleColumnsWidth.put(i, newWidth)
                }
            }
            realColumnsWidth.put(index, column.widthWithMargins)
            realScrollableVirtualWidth = newRealScrollableVirtualWidth
            changed = true
        }

        if (stretchMode && index >= stickyColumnsCount) {
            val scrollableLimitedWidth = tableWidth - stickyWidth
            if (visibleScrollableVirtualWidth == 0 || scrollableLimitedWidth == 0) {
                changed = visibleColumnsWidth[index] == averageStretchColumnWidth
                visibleColumnsWidth.put(index, averageStretchColumnWidth)
            } else {
                val weight = realColumnsWidth[index] / realScrollableVirtualWidth.toFloat()
                val proportionWidth = (scrollableLimitedWidth * weight).toInt()
                changed = visibleColumnsWidth[index] == proportionWidth
                visibleColumnsWidth.put(index, proportionWidth)
            }
            return changed
        }

        if (visibleColumnsWidth[index] < column.widthWithMargins) {
            visibleColumnsWidth.put(index, column.widthWithMargins)
            changed = true
        }

        // force set 0 when column was invisible
        if (!column.visible) {
            visibleColumnsWidth.put(index, 0)
            changed = true
        }

        val shouldCalculateStretchColumnWidth = stretchMode && averageStretchColumnWidth <= 0
        val calculateStretchColumnWidth =
            (changed or shouldCalculateStretchColumnWidth) && index < stickyColumnsCount
        if (calculateStretchColumnWidth) {
            var stickyWidth = 0
            for (i in 0 until stickyColumnsCount) {
                stickyWidth += visibleColumnsWidth[i]
            }
            this.stickyWidth = stickyWidth

            if (stretchMode) {
                var visibleScrollableColumnsCount = 0
                for (i in stickyColumnsCount until columnsCount) {
                    if (isColumnVisible(i)) visibleScrollableColumnsCount++
                }
                if (visibleScrollableColumnsCount > 0) {
                    averageStretchColumnWidth =
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
        return max(0, visibleScrollableVirtualWidth - (tableWidth - stickyWidth))
    }

    fun onColumnsWidthChanged() {
        stickyWidth = 0
        visibleScrollableVirtualWidth = 0
        realScrollableVirtualWidth = 0
        for (i in 0 until columnsCount) {
            if (i < stickyColumnsCount) {
                stickyWidth += visibleColumnsWidth[i]
            } else {
                visibleScrollableVirtualWidth += visibleColumnsWidth[i]
                realScrollableVirtualWidth += realColumnsWidth[i]
            }
        }
        onColumnsWidthWithMarginsChanged?.invoke(layoutManager)
    }

    private fun resetScrollableFirstVisibleColumn() {
        var left = 0
        for (i in stickyColumnsCount until columnsCount) {
            if (left <= scrollX && left + visibleColumnsWidth[i] >= scrollX) {
                scrollableFirstVisibleColumnIndex = i
                scrollableFirstVisibleColumnLeft = left
                return
            }
            left += visibleColumnsWidth[i]
        }
    }

}