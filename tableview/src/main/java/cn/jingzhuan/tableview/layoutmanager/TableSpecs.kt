package cn.jingzhuan.tableview.layoutmanager

import kotlin.math.max
import kotlin.math.min

class TableSpecs(private val layoutManager: ColumnsLayoutManager) {

    var columnsWidth = IntArray(0)
        private set
    var stickyColumnsCount = 0
        private set
    var columnsCount = 0
        private set

    var stretchMode = false

    var scrollX = 0
        internal set

    var stickyWidth = 0
    var scrollableWidth = 0
    var scrollableVirtualWidth = 0

    private var lastDrawStartColumnIndex = 0
    private var lastDrawStartColumnLeft = 0

    private val insetRight = 15F

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
        this.columnsCount = columnsCount
        this.stickyColumnsCount = stickyColumnsCount
        columnsWidth = make(columnsWidth, columnsCount)
    }

    /**
     * @return whether [columnsWidth] have been changed
     */
    internal fun compareAndSetColumnsWidth(index: Int, width: Int): Boolean {
        if (columnsWidth[index] < width) {
            columnsWidth[index] = width
            return true
        }
        return false
    }

    fun computeScrollRange(): Int {
        return max(0F, scrollableWidth - scrollableVirtualWidth + insetRight).toInt()
    }

    internal fun onColumnsWidthChanged() {
        stickyWidth = 0
        scrollableWidth = 0
        for (i in columnsWidth.indices) {
            if (i < stickyColumnsCount) {
                stickyWidth += columnsWidth[i]
            } else {
                scrollableWidth += columnsWidth[i]
            }
        }
        onColumnsWidthWithMarginsChanged?.invoke(layoutManager)
    }

    private fun make(
        original: IntArray,
        newCapacity: Int
    ): IntArray {
        val newArray = IntArray(newCapacity)
        System.arraycopy(original, 0, newArray, 0, min(original.size, newCapacity))
        return newArray
    }

}