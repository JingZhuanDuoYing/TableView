package cn.jingzhuan.tableview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.support.annotation.ColorInt
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.view.ViewGroup
import cn.jingzhuan.tableview.adapter.IRowListAdapterDelegate
import cn.jingzhuan.tableview.adapter.RowListAdapter
import cn.jingzhuan.tableview.adapter.RowListAdapterDelegate
import cn.jingzhuan.tableview.directionlock.DirectionLockRecyclerView
import cn.jingzhuan.tableview.element.HeaderRow
import cn.jingzhuan.tableview.layoutmanager.ColumnsLayoutManager
import cn.jingzhuan.tableview.layoutmanager.RowListLayoutManager
import kotlin.math.max

/**
 * Chenyikang
 * 2018 August 09
 *
 * 通过两个 [RecyclerView] 组合成一个 [ViewGroup]
 * 主要原因是通过 [RecyclerView.LayoutManager] 实现 sticky 效果有损性能
 */
open class TableView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var headerRow: HeaderRow<*>? = null
        private set

    private var adapter: IRowListAdapterDelegate = RowListAdapterDelegate()

    protected val header: RecyclerView =
        DirectionLockRecyclerView(
            context
        )
    protected val main: RecyclerView =
        DirectionLockRecyclerView(
            context
        )

    private val columnsLayoutManager = ColumnsLayoutManager()

    private val scrollListener = RecyclerViewScrollListener()
    var scrolledVerticalCallback: (() -> Unit)?
        set(value) {
            scrollListener.verticalScrollCallback = value
        }
        get() = scrollListener.verticalScrollCallback

    var scrolledHorizontalCallback: (() -> Unit)?
        set(value) {
            scrollListener.horizontalScrollCallback = value
        }
        get() = scrollListener.horizontalScrollCallback

    var scrollingVerticalCallback: ((dy: Int) -> Unit)?
        set(value) {
            scrollListener.verticalScrollingCallback = value
        }
        get() = scrollListener.verticalScrollingCallback

    var scrollingHorizontalCallback: ((dx: Int) -> Unit)?
        set(value) {
            scrollListener.horizontalScrollingCallback = value
        }
        get() = scrollListener.horizontalScrollingCallback

    private val restorePositionController = RestorePositionController()

    private val glowHelper by lazyNone { GlowHelper(this) }

    private val notifyDataSetChangedRunnable by lazyNone {
        object: Runnable {
            override fun run() {
                removeCallbacks(this)
                adapter.notifyDataSetChanged()
            }
        }
    }

    init {
        orientation = VERTICAL

        setAdapter(adapter)
        setStretchMode(false)
        header.layoutManager = RowListLayoutManager(
            context,
            onScrollHorizontallyBy = { dx, _ ->
                headerRow?.layoutManager?.scrollHorizontallyBy(dx) ?: 0
            },
            onHorizontalScrollStateChanged = { state, dx ->
                headerRow?.layoutManager?.onHorizontalScrollStateChanged(state, dx) ?: false
            },
        )
        main.layoutManager = RowListLayoutManager(
            context,
            onScrollHorizontallyBy = { dx, _ ->
                headerRow?.layoutManager?.scrollHorizontallyBy(dx) ?: 0
            },
            onHorizontalScrollStateChanged = { state, dx ->
                headerRow?.layoutManager?.onHorizontalScrollStateChanged(state, dx) ?: false
            },
        )
        header.addOnScrollListener(scrollListener)
        main.addOnScrollListener(scrollListener)
        (header as DirectionLockRecyclerView).directionLockEnabled = true
        (main as DirectionLockRecyclerView).directionLockEnabled = true
        header.itemAnimator = null
        main.itemAnimator = null

        @Suppress("LeakingThis")
        initChildViews()
    }

    open fun initChildViews() {
        addView(header, LayoutParams(MATCH_PARENT, WRAP_CONTENT))
        addView(main, LayoutParams(MATCH_PARENT, MATCH_PARENT))
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        columnsLayoutManager.specs.tableWidth = measuredWidth
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        columnsLayoutManager.specs.tableWidth = width
    }

    fun setHeaderRow(row: HeaderRow<*>?) {
        headerRow = row
        row?.layoutManager = columnsLayoutManager
        columnsLayoutManager.specs.headerRow = row
        if (null != row && columnsLayoutManager.specs.columnsCount == 0 && columnsLayoutManager.specs.stickyColumnsCount == 0) {
            columnsLayoutManager.updateTableSize(row.columns.size, 1)
        }
        adapter.headerRow = row
    }

    fun setStretchMode(isStretch: Boolean) {
        columnsLayoutManager.specs.stretchMode = isStretch
    }

    fun updateTableSize(columns: Int, stickyColumnsCount: Int) {
        updateTableSize(columns, stickyColumnsCount, 0)
    }

    fun updateTableSize(columns: Int, stickyColumnsCount: Int, snapColumnsCount: Int) {
        columnsLayoutManager.updateTableSize(columns, stickyColumnsCount, snapColumnsCount)
    }

    fun setRowsDividerEnabled(
        enable: Boolean,
        @ColorInt color: Int? = null,
        @ColorInt backgroundColor: Int? = null,
        height: Int? = null,
        leftMargin: Int? = null,
        rightMargin: Int? = null,
        skipEndCount: Int? = null
    ) {
        val specs = columnsLayoutManager.specs
        specs.enableRowsDivider = enable
        if (null != color) specs.dividerColor = color

        val headerRowsDivider = if (enable) TableDecoration(
            height ?: specs.dividerStrokeWidth,
            color = specs.dividerColor,
            backgroundColor = backgroundColor ?: Color.TRANSPARENT,
            decorationLeftMargin = leftMargin ?: 0,
            decorationRightMargin = rightMargin ?: 0
        ) else null
        val mainRowsDivider = if (enable) TableDecoration(
            height ?: specs.dividerStrokeWidth,
            color = specs.dividerColor,
            backgroundColor = backgroundColor ?: Color.TRANSPARENT,
            decorationLeftMargin = leftMargin ?: 0,
            decorationRightMargin = rightMargin ?: 0,
            skipEndCount = skipEndCount ?: 0
        ) else null
        setRowsDividerEnabled(enable, headerRowsDivider, mainRowsDivider)
    }

    fun setRowsDividerEnabled(
        enable: Boolean,
        headerDivider: RecyclerView.ItemDecoration?,
        mainDivider: RecyclerView.ItemDecoration?
    ) {
        val specs = columnsLayoutManager.specs
        specs.enableRowsDivider = enable
        if (enable) {
            if (null != headerDivider) {
                if (null != specs.headerRowsDivider) header.removeItemDecoration(specs.headerRowsDivider!!)
                specs.headerRowsDivider = headerDivider
                header.addItemDecoration(headerDivider)
            }
            if (null != mainDivider) {
                if (null != specs.mainRowsDivider) main.removeItemDecoration(specs.mainRowsDivider!!)
                specs.mainRowsDivider = mainDivider
                main.addItemDecoration(mainDivider)
            }
        } else {
            specs.headerRowsDivider = null
            specs.mainRowsDivider = null
            val headerDecorationCount = header.itemDecorationCount
            for (i in 0 until headerDecorationCount) {
                header.removeItemDecorationAt(0)
            }
            val mainDecorationCount = main.itemDecorationCount
            for (i in 0 until mainDecorationCount) {
                main.removeItemDecorationAt(0)
            }
        }
    }

    fun setColumnsDividerEnabled(enable: Boolean) {
        columnsLayoutManager.specs.enableColumnsDivider = enable
    }

    fun setDirectionLockEnabled(enable: Boolean) {
        (header as DirectionLockRecyclerView).directionLockEnabled = enable
        (main as DirectionLockRecyclerView).directionLockEnabled = enable
    }

    fun setCoroutineEnabled(enable: Boolean) {
        columnsLayoutManager.setCoroutineEnabled(enable)
    }

    fun firstVisiblePosition(): Int {
        return (main.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition() ?: -1
    }

    fun lastVisiblePosition(): Int {
        return (main.layoutManager as? LinearLayoutManager)?.findLastVisibleItemPosition() ?: -1
    }

    fun scrollState() = max(header.scrollState, main.scrollState)

    fun isSnapAnimating() = columnsLayoutManager.snapAnimator?.isRunning == true

    fun scrollToPositionWithOffset(
        position: Int,
        offset: Int
    ) {
        (main.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(position, offset)
    }

    fun resetRestorePositionController() {
        restorePositionController.reset()
    }

    // <editor-fold desc="Glow">    ----------------------------------------------------------

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        glowHelper.draw(canvas)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        glowHelper.onInterceptTouchEvent(ev)
        return super.onInterceptTouchEvent(ev)
    }

    override fun onSizeChanged(
        w: Int,
        h: Int,
        oldw: Int,
        oldh: Int
    ) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w != oldw || h != oldh) {
            glowHelper.onSizeChanged()
        }
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int
    ) {
        glowHelper.onNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed)
    }

    override fun onStopNestedScroll(
        target: View
    ) {
        glowHelper.onStopNestedScroll()
    }

    fun setGlowColor(color: Int) {
        glowHelper.glowColor = color
    }

    fun setGlowAlpha(alpha: Float) {
        glowHelper.glowAlpha = alpha
    }

    // </editor-fold desc="Glow">    ---------------------------------------------------------
    fun notifyDataSetChanged() {
        notifyDataSetChanged(true)
    }

    fun notifyDataSetChanged(delayWhenAnimating: Boolean = true) {
        if(isSnapAnimating()) {
            postDelayed(notifyDataSetChangedRunnable, 300)
        } else {
            notifyDataSetChangedRunnable.run()
        }
    }

    private fun setAdapter(adapter: IRowListAdapterDelegate) {
        this.adapter = adapter
        header.setAdapter(true, adapter)
        main.setAdapter(false, adapter)
        restorePositionController.attach(main, main.adapter!!)
    }

    private fun RecyclerView.setAdapter(
        header: Boolean,
        adapterDelegate: IRowListAdapterDelegate
    ) {
        val adapter = RowListAdapter(header)
        adapterDelegate.connect(adapter)
        this.adapter = adapter
    }
}