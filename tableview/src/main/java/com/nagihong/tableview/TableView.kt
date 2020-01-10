package com.nagihong.tableview

import android.content.Context
import android.graphics.Canvas
import androidx.recyclerview.widget.RecyclerView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.nagihong.tableview.adapter.IRowListDelegate
import com.nagihong.tableview.adapter.RowListAdapter
import com.nagihong.tableview.directionlock.DirectionLockRecyclerView
import com.nagihong.tableview.layoutmanager.RowListLayoutManager
import com.nagihong.tableview.layoutmanager.RowListStretchLayoutManager
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

  var adapter: IRowListDelegate? = null
    set(value) {
      field = value
      if (null != value) {
        header.setAdapter(true, value)
        main.setAdapter(false, value)
        restorePositionController.attach(main, main.adapter!!)
      }
    }

  var scrollHorizontalCallback: ((dx: Int, remainingScrollHorizontal: Int) -> Int)? = null
  var scrolledVerticalCallback: (() -> Unit)? = null
  var scrolledHorizontalCallback: (() -> Unit)? = null

  protected val header: RecyclerView =
    DirectionLockRecyclerView(
        context
    )
        .apply {
      directionLockEnabled = true
    }
  protected val main: RecyclerView =
    DirectionLockRecyclerView(
        context
    )
        .apply {
      directionLockEnabled = true
    }

  private val headerLayoutManager =
    RowListLayoutManager(context) { dx, remainingScrollHorizontal ->
      scrollHorizontalCallback?.invoke(
          dx, remainingScrollHorizontal
      ) ?: 0
    }
  private val mainLayoutManager =
    RowListLayoutManager(context) { dx, remainingScrollHorizontal ->
      scrollHorizontalCallback?.invoke(
          dx, remainingScrollHorizontal
      ) ?: 0
    }
  private val headerStretchLayoutManager by lazyNone { RowListStretchLayoutManager(context) }
  private val mainStretchLayoutManager by lazyNone { RowListStretchLayoutManager(context) }

  private val scrollListener = RecyclerViewScrollListener(
      verticalScrollCallback = { scrolledVerticalCallback?.invoke() },
      horizontalScrollCallback = { scrolledHorizontalCallback?.invoke() }
  )
  private val restorePositionController = RestorePositionController()

  private val glowHelper by lazyNone { GlowHelper(this) }

  init {
    orientation = VERTICAL

    @Suppress("LeakingThis")
    initChildViews()

    setStretchMode(false)
    header.addOnScrollListener(scrollListener)
    main.addOnScrollListener(scrollListener)
    header.itemAnimator = null
    main.itemAnimator = null
  }

  open fun initChildViews() {
    addView(header, LayoutParams(MATCH_PARENT, WRAP_CONTENT))
    addView(main, LayoutParams(MATCH_PARENT, MATCH_PARENT))
  }

  fun setStretchMode(isStretch: Boolean) {
    if (isStretch) {
      header.layoutManager = headerStretchLayoutManager
      main.layoutManager = mainStretchLayoutManager
    } else {
      header.layoutManager = headerLayoutManager
      main.layoutManager = mainLayoutManager
    }
  }

  fun firstVisiblePosition(): Int {
    return (main.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition() ?: -1
  }

  fun lastVisiblePosition(): Int {
    return (main.layoutManager as? LinearLayoutManager)?.findLastVisibleItemPosition() ?: -1
  }

  fun scrollState() = max(header.scrollState, main.scrollState)

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

  private fun RecyclerView.setAdapter(
    header: Boolean,
    adapterDelegate: IRowListDelegate
  ) {
    val adapter = RowListAdapter(header)
    adapterDelegate.connect(adapter)
    this.adapter = adapter
  }
}