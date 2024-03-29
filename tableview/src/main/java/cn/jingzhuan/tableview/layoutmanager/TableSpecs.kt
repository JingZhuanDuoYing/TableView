package cn.jingzhuan.tableview.layoutmanager

import android.graphics.Paint
import androidx.recyclerview.widget.RecyclerView
import android.util.SparseIntArray
import androidx.annotation.ColorInt
import cn.jingzhuan.tableview.element.Column
import cn.jingzhuan.tableview.element.HeaderRow
import timber.log.Timber
import kotlin.math.max

class TableSpecs(private val layoutManager: ColumnsLayoutManager) {

    var enableCoroutine = false

    var headerRow: HeaderRow<*>? = null

    // 列显示宽度
    val visibleColumnsWidth = SparseIntArray()

    // 列实际测量宽度
    private val realColumnsWidth = SparseIntArray()

    var stickyColumnsCount = 0
        private set

    var snapColumnsCount = 0
        private set

    var columnsCount = 0
        private set

    var stretchMode = false
    private var averageStretchColumnWidth = 0

    var scrollX = 0
        private set

    var independentScrollX = 0
        internal set

    /**
     * help to locate column by coordinate
     */
    internal var isScrollableFirstVisibleMarkValid = false
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

        if (isScrollableFirstVisibleMarkValid &&
            scrollableFirstVisibleColumnLeft <= scrollX
            && scrollableFirstVisibleColumnLeft + visibleColumnsWidth[scrollableFirstVisibleColumnIndex] >= scrollX
        ) {
            // 最左面的字段没有发生变化
            return
        }

        if (!isScrollableFirstVisibleMarkValid) {
            resetScrollableFirstVisibleColumn()
        }

        if (dx > 0) {
            var left =
                scrollableFirstVisibleColumnLeft + visibleColumnsWidth[scrollableFirstVisibleColumnIndex]
            for (i in scrollableFirstVisibleColumnIndex + 1 until columnsCount) {
                if (scrollX in left until left + visibleColumnsWidth[i]) {
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
                if (scrollX in left until left + visibleColumnsWidth[i]) {
                    scrollableFirstVisibleColumnIndex = i
                    scrollableFirstVisibleColumnLeft = left
                    return
                }
            }
        }

        // fallback
        resetScrollableFirstVisibleColumn()
    }

    internal fun updateTableSize(
        columnsCount: Int,
        stickyColumnsCount: Int,
        snapColumnsCount: Int = 0
    ) {
        if (stickyColumnsCount > columnsCount) throw IllegalArgumentException("stickyColumnsCount must not be greater than columnsCount")
        this.columnsCount = columnsCount
        this.stickyColumnsCount = stickyColumnsCount
        this.snapColumnsCount = snapColumnsCount
        // 列数变化，重置 stretchMode 列宽
        this.averageStretchColumnWidth = 0
        visibleColumnsWidth.clear()
        realColumnsWidth.clear()
        stickyWidth = 0
        if (scrollX < 0 && snapColumnsCount <= 0) scrollX = 0
        onColumnsWidthChanged()
        resetScrollableFirstVisibleColumn()
    }

    fun compareAndSetStretchColumnsWidth() {
        val stretchWidth = tableWidth - stickyWidth
        for (i in stickyColumnsCount + snapColumnsCount until columnsCount) {
            val weight = realColumnsWidth[i] / realScrollableVirtualWidth.toFloat()
            visibleColumnsWidth.put(i, (weight * stretchWidth).toInt())
        }
    }

    /**
     * @return whether snap column's width changed or not
     */
    fun compareAndSetSnapColumnsWidth(): Boolean {
        if (snapColumnsCount <= 0) return false
        val snapWidth = getSnapWidth()
        var flexibleWidth = snapWidth
        val flexibleWeights = mutableMapOf<Int, Int>()
        var totalWeight = 0F
        var changed = false
        for (i in stickyColumnsCount until stickyColumnsCount + snapColumnsCount) {
            val weight = headerRow?.columns?.getOrNull(i)?.weight ?: 0
            if (isColumnVisible(i)) {
                flexibleWeights[i] = weight
                totalWeight += weight
            } else {
                flexibleWeights[i] = 0
            }
            if (weight == 0 && isColumnVisible(i)) {
                flexibleWidth -= visibleColumnsWidth[i]
            }
        }
        for (i in stickyColumnsCount until stickyColumnsCount + snapColumnsCount) {
            val weight = flexibleWeights[i] ?: 0
            if (weight <= 0) continue
            val newWidth = (flexibleWidth * (weight / totalWeight)).toInt()
            if (newWidth != visibleColumnsWidth[i]) {
                visibleColumnsWidth.put(i, newWidth)
                changed = true
            }
        }
        return changed
    }

    /**
     * @return whether [visibleColumnsWidth] have been changed
     */
    internal fun compareAndSetColumnsWidth(index: Int, column: Column): Boolean {
        var changed = false

        if (realColumnsWidth[index] < column.widthWithMargins) {
            val expand = column.widthWithMargins - realColumnsWidth[index]
            val newRealScrollableVirtualWidth = realScrollableVirtualWidth + expand
            realColumnsWidth.put(index, column.widthWithMargins)
            realScrollableVirtualWidth = newRealScrollableVirtualWidth
            changed = true
        }

        if (stretchMode && index >= stickyColumnsCount) {
            if (isColumnVisible(index)) {
                if (visibleScrollableVirtualWidth == 0 || visibleColumnsWidth[index] <= 0) {
                    changed = visibleColumnsWidth[index] == averageStretchColumnWidth
                    visibleColumnsWidth.put(index, averageStretchColumnWidth)
                }
            } else {
                if (visibleScrollableVirtualWidth == 0 || visibleColumnsWidth[index] != 0) {
                    changed = true
                    visibleColumnsWidth.put(index, 0)
                }
            }
            return changed
        }

        if (visibleColumnsWidth[index] < column.widthWithMargins) {
            visibleColumnsWidth.put(index, column.widthWithMargins)
            changed = true
        }

        // force set 0 when column was invisible
        if (!isColumnVisible(index)) {
            visibleColumnsWidth.put(index, 0)
            changed = true
        }

        val shouldCalculateStretchColumnWidth =
            index < stickyColumnsCount && stretchMode && averageStretchColumnWidth <= 0
        if (shouldCalculateStretchColumnWidth && index == stickyColumnsCount - 1) changed = true

        if (changed or shouldCalculateStretchColumnWidth) {
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

        return changed
    }

    internal fun isColumnVisible(index: Int): Boolean {
        return headerRow?.columns?.getOrNull(index)?.visible ?: true
    }

    fun computeScrollRange(): Int {
        if (stretchMode) return 0
        val times = if (snapColumnsCount > 0) 2 else 1
        return max(0, visibleScrollableVirtualWidth - (tableWidth - stickyWidth) * times)
    }

    fun onColumnsWidthChanged() {
        var newStickyWidth = 0
        var newVisibleScrollVirtualWidth = 0
        var newRealScrollableVirtualWidth = 0

        for (i in 0 until columnsCount) {
            if (i < stickyColumnsCount) {
                newStickyWidth += visibleColumnsWidth[i]
            } else {
                newVisibleScrollVirtualWidth += visibleColumnsWidth[i]
                newRealScrollableVirtualWidth += realColumnsWidth[i]
            }
        }

        stickyWidth = newStickyWidth
        visibleScrollableVirtualWidth = newVisibleScrollVirtualWidth
        realScrollableVirtualWidth = newRealScrollableVirtualWidth

        onColumnsWidthWithMarginsChanged?.invoke(layoutManager)
    }

    internal fun resetScrollableFirstVisibleColumn() {
        val snapWidth = getSnapWidth()
        var left: Int
        val startColumnIndex: Int
        if (snapColumnsCount > 0 && snapWidth <= 0) {
            startColumnIndex = stickyColumnsCount + snapColumnsCount
            left = 0
            isScrollableFirstVisibleMarkValid = false
        } else {
            startColumnIndex = stickyColumnsCount
            left = -snapWidth
        }
        for (i in startColumnIndex until columnsCount) {
            val right = left + visibleColumnsWidth[i]
            if (scrollX in left until right) {
                scrollableFirstVisibleColumnIndex = i
                scrollableFirstVisibleColumnLeft = left
                isScrollableFirstVisibleMarkValid = true
                return
            }
            left += visibleColumnsWidth[i]
        }
        scrollableFirstVisibleColumnIndex = stickyColumnsCount + snapColumnsCount
        scrollableFirstVisibleColumnLeft = 0
    }

    internal fun getSnapWidth(): Int {
        return if (snapColumnsCount <= 0) 0 else max(0, tableWidth - stickyWidth)
    }

    internal fun isSnapWeightColumn(index: Int): Boolean {
        return index in stickyColumnsCount until stickyColumnsCount + snapColumnsCount
                && headerRow?.columns?.getOrNull(index)?.weight ?: 0 > 0
    }

}