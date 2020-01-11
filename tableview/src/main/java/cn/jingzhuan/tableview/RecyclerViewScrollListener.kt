package cn.jingzhuan.tableview

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

internal class RecyclerViewScrollListener(
  private val verticalScrollCallback: () -> Unit,
  private val horizontalScrollCallback: () -> Unit
) : RecyclerView.OnScrollListener() {

  /**
   * 如果当次滑动后，停留位置与上次一样，则不刷新当前页面内容数据
   */
  private var lastIdlePosition = 0
  private var scrolledHorizontally = false

  override fun onScrolled(
    recyclerView: RecyclerView,
    dx: Int,
    dy: Int
  ) {
    super.onScrolled(recyclerView, dx, dy)
    if (dx != 0) scrolledHorizontally = true
  }

  override fun onScrollStateChanged(
    recyclerView: RecyclerView,
    newState: Int
  ) {
    super.onScrollStateChanged(recyclerView, newState)
    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
      val scrolledVertically = lastIdlePosition != findFirstVisibleItemPosition(recyclerView)
      if (scrolledVertically) {
        verticalScrollCallback.invoke()
      } else if (scrolledHorizontally) {
        horizontalScrollCallback.invoke()
      }
      lastIdlePosition = findFirstVisibleItemPosition(recyclerView)
      scrolledHorizontally = false
    }
  }

  private fun findFirstVisibleItemPosition(recyclerView: RecyclerView): Int {
    return (recyclerView.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition()
        ?: -1
  }
}