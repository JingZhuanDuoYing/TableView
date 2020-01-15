package cn.jingzhuan.tableview.layoutmanager

import android.graphics.Paint
import android.util.SparseIntArray
import androidx.annotation.ColorInt
import kotlin.math.max

class TableSpecs(private val layoutManager: ColumnsLayoutManager) {

    val columnsWidth = SparseIntArray()

    var stickyColumnsCount = 0
        private set
    var columnsCount = 0
        private set

    var stretchMode = false

    var scrollX = 0
        internal set

    var width = 0
        internal set
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

    private var lastDrawStartColumnIndex = 0
    private var lastDrawStartColumnLeft = 0

    private val insetRight = 15F

    internal val columnsDividerPaint = Paint().apply {
        isDither = true
        isAntiAlias = true
        color = dividerColor
    }

    @Transient
    var onColumnsWidthWithMarginsChanged: ((ColumnsLayoutManager) -> Unit)? = null

    @Synchronized
    internal fun getScrollableColumnLeftByIndex(index: Int): Int {
        if (lastDrawStartColumnIndex == index) return lastDrawStartColumnLeft
        var tempLeft = 0
        for (i in stickyColumnsCount until index) {
            tempLeft += columnsWidth[i]
        }
        lastDrawStartColumnIndex = index
        lastDrawStartColumnLeft = tempLeft
        return tempLeft
    }

    internal fun updateTableSize(columnsCount: Int, stickyColumnsCount: Int) {
        if(stickyColumnsCount > columnsCount) throw IllegalArgumentException("stickyColumnsCount must not be greater than columnsCount")
        this.columnsCount = columnsCount
        this.stickyColumnsCount = stickyColumnsCount
    }

    /**
     * @return whether [columnsWidth] have been changed
     */
    internal fun compareAndSetColumnsWidth(index: Int, width: Int): Boolean {
        if (columnsWidth[index] < width) {
            columnsWidth.put(index, width)
            if(index < stickyColumnsCount) {
                var stickyWidth = 0
                for(i in 0 until stickyColumnsCount) {
                    stickyWidth += columnsWidth[i]
                }
                this.stickyWidth = stickyWidth
            }
            return true
        }
        return false
    }

    fun computeScrollRange(): Int {
        return max(0F, scrollableVirtualWidth - (width - stickyWidth) + insetRight).toInt()
    }

    internal fun onColumnsWidthChanged() {
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

}