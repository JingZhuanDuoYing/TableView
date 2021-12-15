package cn.jingzhuan.tableview.layoutmanager

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import cn.jingzhuan.tableview.*
import cn.jingzhuan.tableview.element.DrawableColumn
import cn.jingzhuan.tableview.element.Row
import cn.jingzhuan.tableview.element.ViewColumn
import cn.jingzhuan.tableview.firstOrNullSafer
import cn.jingzhuan.tableview.lazyNone
import cn.jingzhuan.tableview.runOnMainThread
import timber.log.Timber
import java.io.ObjectInputStream
import java.io.Serializable
import java.util.*
import kotlin.collections.LinkedHashMap
import kotlin.collections.LinkedHashSet
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

class ColumnsLayoutManager : Serializable {

    internal val specs by lazyNone { TableSpecs(this) }

    @Transient
    private var attachedRows = LinkedHashSet<IRowLayout>(26)

    @Transient
    private var attachedIndependentScrollRows = LinkedHashSet<IRowLayout>(26)

    @Transient
    internal var snapAnimator: ValueAnimator? = null

    private val runnable = Runnable {
        attachedRows.forEachSafe { it.doLayout() }
    }

    init {
        specs.onColumnsWidthWithMarginsChanged = OnColumnsWidthWithMarginsChanged@{
            val parent = attachedRows.firstOrNullSafer()?.onGetParentView() as? View
                ?: return@OnColumnsWidthWithMarginsChanged
            parent.removeCallbacks(runnable)
            parent.post(runnable)
        }
    }

    private fun readObject(inputStream: ObjectInputStream) {
        inputStream.defaultReadObject()
        attachedRows = LinkedHashSet(26)
        attachedIndependentScrollRows = LinkedHashSet(26)
    }

    fun updateTableSize(
        columnsSize: Int = this.specs.columnsCount,
        stickyColumns: Int = this.specs.stickyColumnsCount
    ) {
        updateTableSize(columnsSize, stickyColumns, 0)
    }

    fun updateTableSize(
        columnsSize: Int = this.specs.columnsCount,
        stickyColumns: Int = this.specs.stickyColumnsCount,
        snapColumnsCount: Int = 0
    ) {
        specs.updateTableSize(columnsSize, stickyColumns, snapColumnsCount)
        attachedRows.forEachSafe {
            it.updateScrollX(specs.scrollX)
            it.onGetRow()?.forceLayout = true
        }
    }

    fun setCoroutineEnabled(enable: Boolean) {
        specs.enableCoroutine = enable
    }

    fun randomRowLayout(): IRowLayout? {
        return attachedRows.firstOrNullSafer()
    }

    fun forceDetachAllRowLayouts() {
        attachedRows.clear()
    }

    fun attachRowLayout(layout: IRowLayout) {
        if (layout.isIndependentScrollRange()) {
            layout.updateScrollX(specs.independentScrollX)
        } else {
            layout.updateScrollX(specs.scrollX)
        }
        attachedRows.addSafer(layout)
        if (layout.isIndependentScrollRange()) attachedIndependentScrollRows.addSafer(layout)
    }

    fun detachRowLayout(layout: IRowLayout) {
        attachedRows.removeSafer(layout)
        if (layout.isIndependentScrollRange()) {
            attachedIndependentScrollRows.removeSafer(layout)
            calibrateIndependentScrollX()
        }
    }

    fun containsRowLayout(layout: IRowLayout): Boolean {
        return attachedRows.containsSafer(layout)
    }

    fun scrollHorizontallyBy(dx: Int): Int {
        if (attachedRows.isEmpty()) return 0
        val scrollRange = specs.computeScrollRange()
        val scrollDiff = specs.scrollX - specs.independentScrollX
        val preConsumed =
            if (attachedIndependentScrollRows.isNotEmpty() && dx < 0 && scrollDiff in (dx until 0)) {
                scrollDiff
            } else {
                0
            }
        val consumed = when {
            // 特殊逻辑
            attachedIndependentScrollRows.isNotEmpty() && dx < 0 && scrollDiff < 0 -> {
                // 向左滚动事件消耗完毕后，独立滚动业务还有余量
                if (scrollDiff < dx) 0
                // 向左滚动事件消耗完毕后，独立滚动业务没有余量
                else dx - preConsumed
            }
            // 特殊逻辑：向左滚动事件消耗完毕后，统一滚动业务没有余量
            specs.scrollX > 0 && dx < 0 && specs.scrollX < dx.absoluteValue -> -specs.scrollX
            // 特殊逻辑：向右滚动事件消耗完毕后，统一滚动业务没有余量
            specs.scrollX < 0 && dx > 0 && specs.scrollX.absoluteValue < dx -> specs.scrollX.absoluteValue
            // 普通逻辑
            dx > 0 -> {
                val maxDx = scrollRange - specs.scrollX
                min(dx, maxDx)
            }
            // 普通逻辑
            dx < 0 -> {
                val minDx = -specs.scrollX - specs.getSnapWidth()
                max(dx, minDx)
            }
            else -> 0
        }
        if (consumed == 0) {
            // 这里只执行独立滚动逻辑，不需要考虑 preConsumed
            return independentScrollHorizontallyBy(dx)
        }

        // 更新值
        val expectScrollX = specs.scrollX + consumed
        if (expectScrollX > scrollRange) {
            specs.updateScrollX(scrollRange)
        } else {
            specs.updateScrollX(expectScrollX)
        }

        // 调整当前持有的所有RowLayout, 计算独立滚动业务消耗值，并更新独立滚动业务的标准值
        var independentConsumed = 0
        attachedRows.forEachSafe {
            if (it.isIndependentScrollRange()) {
                val rowConsumed = independentScrollHorizontallyBy(it, dx)
                if (dx < 0 && rowConsumed < independentConsumed) independentConsumed = rowConsumed
                if (dx > 0 && rowConsumed > independentConsumed) independentConsumed = rowConsumed
            } else {
                it.onScrollTo(specs.scrollX, 0)
            }
        }

        //
        if (attachedIndependentScrollRows.isEmpty()) {
            independentConsumed = consumed
            specs.independentScrollX = specs.scrollX
        } else {
            specs.independentScrollX += independentConsumed
        }

        // 由于两者的滚动逻辑是完全独立的，这里需要返回两者滚动消耗的最大(小)值，否则会影响后续的 fling
        val value = if (dx > 0) {
            max(independentConsumed, consumed + preConsumed)
        } else {
            min(independentConsumed, consumed + preConsumed)
        }
        Log.d(
            "12345 ",
            "12345 dx: $dx, value: $value, independentConsumed: $independentConsumed, consumed: $consumed, preConsumed: $preConsumed, scrollDiff: $scrollDiff"
        )
        return value
    }

    private fun independentScrollHorizontallyBy(dx: Int): Int {
        var consumed = 0
        attachedIndependentScrollRows.forEachSafe {
            val rowConsumed = independentScrollHorizontallyBy(it, dx)
            if (dx < 0 && rowConsumed < consumed) consumed = rowConsumed
            if (dx > 0 && rowConsumed > consumed) consumed = rowConsumed
        }
        Log.d(
            "12345",
            "12345 dx: $dx, independentScrollHorizontallyBy $dx, oldIndependentScrollX: ${specs.independentScrollX}, consumed: $consumed"
        )
        specs.independentScrollX += consumed
        return consumed
    }

    private fun independentScrollHorizontallyBy(rowLayout: IRowLayout, dx: Int): Int {
        val oldScrollX = rowLayout.onGetScrollX()
        var fixedDx = dx
        if(dx < 0 && oldScrollX > -dx) {
            if(oldScrollX - specs.independentScrollX < dx) return 0
            fixedDx = dx + (specs.independentScrollX - oldScrollX)
        }
        rowLayout.onScrollBy(fixedDx)
        return rowLayout.onGetScrollX() - oldScrollX
    }

    private fun calibrateIndependentScrollX() {
        val x = attachedIndependentScrollRows.map { it.onGetScrollX() }.maxOrNull() ?: specs.independentScrollX
        Log.d("12345", "12345 calibrateIndependentScrollX $x, old: ${specs.independentScrollX}")
        specs.independentScrollX = x
    }

    /**
     * @return 返回 true 会停止 RecyclerView 的 ViewFlinger，然后可以开始执行 TableView 的 SnapScroll
     */
    @SuppressLint("Recycle")
    internal fun onHorizontalScrollStateChanged(state: Int, dx: Int): Boolean {
        if (specs.snapColumnsCount <= 0) return false
        if (state == RecyclerView.SCROLL_STATE_SETTLING && specs.scrollX < 0) {
            val snapWidth = specs.getSnapWidth()
            if (snapWidth <= 0) return false
            snapAnimator?.cancel()
            val endX = if (dx > 0) 0 else -snapWidth
            val animator = ValueAnimator.ofInt(specs.scrollX, endX)
            animator.interpolator = AccelerateDecelerateInterpolator()
            animator.addUpdateListener {
                val x = it.animatedValue as? Int ?: return@addUpdateListener
                val animateDx = x - specs.scrollX
                scrollHorizontallyBy(animateDx)
            }
            snapAnimator = animator
            animator.start()
            return true
        } else if (state == RecyclerView.SCROLL_STATE_IDLE && specs.scrollX < 0) {
            if (snapAnimator?.isRunning == true) return true
            val snapWidth = specs.getSnapWidth()
            if (snapWidth <= 0) return false
            snapAnimator?.cancel()
            val endX = if (snapWidth + specs.scrollX > -specs.scrollX) 0 else -snapWidth
            val animator = ValueAnimator.ofInt(specs.scrollX, endX)
            animator.interpolator = AccelerateDecelerateInterpolator()
            animator.addUpdateListener {
                val x = it.animatedValue as? Int ?: return@addUpdateListener
                val animateDx = x - specs.scrollX
                scrollHorizontallyBy(animateDx)
            }
            snapAnimator = animator
            animator.start()
        }
        return false
    }

    internal fun animateSnapColumnsDemonstration(
        depth: Int,
        enterDuration: Long,
        stayDuration: Long,
        exitDuration: Long
    ) {
        if (specs.snapColumnsCount <= 0) return
        val snapWidth = specs.getSnapWidth()
        if (snapWidth <= 0) return
        if (snapAnimator?.isRunning == true) return
        snapAnimator?.cancel()
        val duration = enterDuration + stayDuration + exitDuration
        val animator = ValueAnimator.ofInt(0, duration.toInt())
        animator.duration = duration
        animator.addUpdateListener {
            val current = it.animatedValue as Int
            if (current < enterDuration) {
                val progress = current / enterDuration.toFloat()
                val scrollX = depth * progress * -1F
                val dx = scrollX - specs.scrollX
                scrollHorizontallyBy(dx.toInt())
            } else if (current >= enterDuration + stayDuration) {
                val progress = (current - enterDuration - stayDuration) / exitDuration.toFloat()
                val scrollX = depth * (1 - progress) * -1F
                val dx = scrollX - specs.scrollX
                scrollHorizontallyBy(dx.toInt())
            }
        }
        snapAnimator = animator
        animator.start()
    }

    private fun adjustSnapScrollXAfterColumnsWidthChanged() {
        if (specs.scrollX >= 0) return
        val snapWidth = specs.getSnapWidth()
        if (snapWidth <= 0) return
        val endX = if (snapWidth + specs.scrollX > -specs.scrollX) 0 else -snapWidth
        if (specs.scrollX == endX) return
        val dx = endX - specs.scrollX
        scrollHorizontallyBy(dx)
    }

    internal fun measureAndLayout(
        context: Context,
        row: Row<*>,
        rowLayout: RowLayout,
        scrollableContainer: ViewGroup,
        layoutOnly: Boolean = false
    ) {
        // 如果rowLayout持有scrollableContainer，说明rowLayout之前已经初始化过了，不需要重新创建View对象
        val initialized = rowLayout.indexOfChild(scrollableContainer) >= 0

        var pendingLayout = false

        if (row.rowHeight <= 0 && !layoutOnly) {
            // measure drawable columns first of all
            if (row.measure(context, specs)) pendingLayout = true
            if (row.rowHeight <= 0) {
                row.rowHeight = rowLayout.height
            }
        }

        // 普通View的Measure/Layout流程
        val maxSize = min(row.columns.size, specs.columnsCount)
        if (!layoutOnly) {
            var scrollableViewIndex = -1
            var stickyViewIndex = -1
            for (index in 0 until maxSize) {
                val column = row.columns[index]

                val sticky = index < specs.stickyColumnsCount
                val snap = (index - specs.stickyColumnsCount) < specs.snapColumnsCount
                val visible = specs.isColumnVisible(index)

                if (column is DrawableColumn) {
                    if (!visible) continue
                    // this may happens when columns changed
                    val heightNotDetermined =
                        column.heightWithMargins == 0 && column.height != ViewGroup.LayoutParams.MATCH_PARENT
                    if (row.forceLayout || column.widthWithMargins == 0 || heightNotDetermined || specs.visibleColumnsWidth[index] == 0) {
                        // measure drawable column in necessary
                        if (row.measure(context, specs)) pendingLayout = true
                    }
                    continue
                }

                // 对于非ViewColumn，如DrawableColumn等，不需要走如下流程
                if (column !is ViewColumn) continue

                if (sticky) ++stickyViewIndex else ++scrollableViewIndex

                // 未初始化过的，需要新建View对象
                val view =
                    if (initialized) {
                        (if (sticky) rowLayout.getChildAt(stickyViewIndex)
                            ?: column.createView(context)
                        else scrollableContainer.getChildAt(scrollableViewIndex))
                            ?: column.createView(context)
                    } else {
                        column.createView(context)
                    }

                if (!initialized) {
                    // 未初始化过，需要将新创建的View添加到ViewGroup
                    rowLayout.runOnMainThread {
                        if (sticky) {
                            rowLayout.addView(view)
                        } else {
                            scrollableContainer.addView(view)
                        }
                    }
                } else {
                    val addView = (sticky && rowLayout.indexOfChild(view) < 0)
                            || (!sticky && scrollableContainer.indexOfChild(view) < 0)
                    if (addView) {
                        rowLayout.runOnMainThread {
                            if (sticky) rowLayout.addView(
                                view,
                                (rowLayout.childCount - 2).coerceAtLeast(0)
                            )
                            else scrollableContainer.addView(view)
                        }
                        pendingLayout = true
                    }
                }

                val visibilityChanged = (visible && view.visibility != View.VISIBLE)
                        || (!visible && view.visibility != View.GONE)
                if (visibilityChanged) {
                    pendingLayout = true
                }

                view.visibility = if (visible) View.VISIBLE else View.GONE
                if (!visible) {
                    column.widthWithMargins = 0
                    if (specs.compareAndSetColumnsWidth(index, column)) {
                        pendingLayout = true
                    }
                    continue
                }

                // 绑定column和view
                rowLayout.runOnMainThread {
                    column.bindView(view, row)
                }

                if (view.measuredWidth <= 0 || view.measuredHeight <= 0 || row.forceLayout || column.forceLayout || visibilityChanged) {
                    if (specs.isSnapWeightColumn(index) && specs.visibleColumnsWidth[index] > 0) {
                        view.layoutParams.width = specs.visibleColumnsWidth[index]
                    }
                    // 实际Measure
                    column.measureView(view)
                }

                if (column.forceLayout) {
                    pendingLayout = true
                }

                if (column.heightWithMargins > row.rowHeight) {
                    row.rowHeight = column.heightWithMargins
                    pendingLayout = true
                }

                if (specs.compareAndSetColumnsWidth(index, column)) {
                    pendingLayout = true
                }

                if (!column.checkLayout(view)) {
                    pendingLayout = true
                }
            }

            if (initialized) {
                val prePendingRemoveStickyCount = rowLayout.realChildCount() - 2 - stickyViewIndex
                val prePendingRemoveScrollableCount =
                    scrollableContainer.childCount - 1 - scrollableViewIndex
                if (prePendingRemoveStickyCount > 0 || prePendingRemoveScrollableCount > 0) {
                    rowLayout.runOnMainThread {
                        val pendingRemoveStickyCount =
                            rowLayout.realChildCount() - 2 - stickyViewIndex
                        val pendingRemoveScrollableCount =
                            scrollableContainer.childCount - 1 - scrollableViewIndex
                        for (i in 0 until pendingRemoveStickyCount) {
                            rowLayout.removeViewAt(rowLayout.realChildCount() - 2)
                        }
                        for (i in 0 until pendingRemoveScrollableCount) {
                            scrollableContainer.removeViewAt(scrollableContainer.childCount - 1)
                        }
                    }
                }
            }
        }

        if (!initialized) {
            // 未初始化过，需要将scrollableContainer添加进rowLayout
            rowLayout.runOnMainThread {
                if (scrollableContainer.parent == null) rowLayout.addView(scrollableContainer)
            }
        }

        // 列宽发生变化或者第一次初始化，都需要Layout
        if (layoutOnly || pendingLayout || !initialized || row.forceLayout) {
            if (specs.stretchMode) specs.compareAndSetStretchColumnsWidth()
            if (specs.compareAndSetSnapColumnsWidth()) measureSnapViewColumns()
            if (pendingLayout) specs.resetScrollableFirstVisibleColumn()
            row.layout(context, specs)

            var viewIndex = 0
            row.columns.forEachIndexed { _, column ->
                if (column !is ViewColumn) return@forEachIndexed
                val currentViewIndex = viewIndex
                viewIndex++
                val view = rowLayout.getChildAt(currentViewIndex) ?: return@forEachIndexed
                column.layoutView(view, column.forceLayout)
                column.forceLayout = false
            }

            if (!row.forceLayoutLock) row.forceLayout = false
            // layoutOnly 模式不调用，因为并没有执行任何实际的 measure，不能确定列宽是否变化
            if (pendingLayout) specs.onColumnsWidthChanged()
        }

        // scrollableContainer检查是否需要Measure/Layout
        measureAndLayoutScrollableContainer(
            context,
            row,
            rowLayout,
            scrollableContainer,
            layoutOnly
        )

        // 校准scrollX
        val scrollRange = specs.computeScrollRange()
        if (specs.scrollX > scrollRange && scrollRange > 0) specs.updateScrollX(scrollRange)
        if (scrollableContainer.scrollX != specs.scrollX) {
            scrollableContainer.scrollTo(specs.scrollX, 0)
        } else {
            scrollableContainer.postInvalidate()
        }

        if (pendingLayout) adjustSnapScrollXAfterColumnsWidthChanged()
    }

    /**
     * 检查ScrollableContainer是否需要Measure/Layout，并执行
     */
    private fun measureAndLayoutScrollableContainer(
        context: Context,
        row: Row<*>,
        rowLayout: RowLayout,
        scrollableContainer: ViewGroup,
        layoutOnly: Boolean = false
    ): Int {
        val scrollableContainerWidth = specs.tableWidth - specs.stickyWidth
        val rowHeight = row.getRowHeight(context)

        // 实际宽度发生变化重新执行Measure
        if (!layoutOnly) {
            if (scrollableContainer.width != scrollableContainerWidth || scrollableContainer.height != rowHeight) {
                val widthMeasureSpec =
                    MeasureSpec.makeMeasureSpec(
                        max(0, scrollableContainerWidth),
                        MeasureSpec.EXACTLY
                    )
                val heightMeasureSpec = MeasureSpec.makeMeasureSpec(rowHeight, MeasureSpec.EXACTLY)
                scrollableContainer.measure(widthMeasureSpec, heightMeasureSpec)
            }
        }

        // 检查是否需要Layout，并执行
        val left = rowLayout.paddingLeft + specs.stickyWidth
        val right = left + scrollableContainer.measuredWidth
        if (layoutOnly || scrollableContainer.left != left || scrollableContainer.top != 0 || scrollableContainer.right != right || scrollableContainer.bottom != rowHeight) {
            scrollableContainer.layout(left, 0, right, rowHeight)
        }
        return scrollableContainer.measuredWidth
    }

    private fun measureSnapViewColumns() {
        if (specs.snapColumnsCount <= 0) return
        attachedRows.forEachSafe {
            var viewIndex = 0
            @Suppress("UseWithIndex")
            for (i in 0 until specs.stickyColumnsCount + specs.snapColumnsCount) {
                val column = it.onGetRow()?.columns?.getOrNull(i) ?: return@forEachSafe
                if (column !is ViewColumn) continue
                val currentViewIndex = viewIndex
                viewIndex++
                if (i < specs.stickyColumnsCount) continue
                val view = it.onGetChildAt(currentViewIndex) ?: return@forEachSafe
                view.layoutParams.width = specs.visibleColumnsWidth[i]
                column.measureView(view)
            }
        }
    }

}