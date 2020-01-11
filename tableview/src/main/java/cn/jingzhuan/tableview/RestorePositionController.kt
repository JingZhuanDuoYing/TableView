package cn.jingzhuan.tableview

import android.graphics.Rect
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.jingzhuan.tableview.adapter.AdapterDataObserver

internal class RestorePositionController: RecyclerView.OnScrollListener() {

  var enabled = true

  private var recyclerView: RecyclerView? = null

  private val observer =
    AdapterDataObserver {
      restoreLastIdlePositionAndOffset(recyclerView)
    }

  private var markFlag = false

  private var lastIdlePosition = 0
  private var lastIdlePositionOffset = 0

  private val rect by lazyNone { Rect() }

  private var lastRestoreRunnable: RestoreRunnable? = null

  fun attach(recyclerView: RecyclerView, adapter: RecyclerView.Adapter<*>) {
    this.recyclerView = recyclerView
    adapter.registerAdapterDataObserver(observer)
  }

  fun reset() {
    if(null != lastRestoreRunnable) recyclerView?.removeCallbacks(lastRestoreRunnable)
    lastIdlePosition = 0
    lastIdlePositionOffset = 0
  }

  override fun onScrollStateChanged(
    recyclerView: RecyclerView,
    newState: Int
  ) {
    super.onScrollStateChanged(recyclerView, newState)
    if(!enabled) return
    if (newState != RecyclerView.SCROLL_STATE_IDLE) return
    markIdlePosition(recyclerView)
  }

  private fun markIdlePosition(recyclerView: RecyclerView?) {
    recyclerView ?: return
    val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
    val position = layoutManager.findFirstVisibleItemPosition()

    lastIdlePosition = position
    markFlag = true
    val view = layoutManager.findViewByPosition(position)

    /*
        获取不到 offset 的话，将其置0,
        至少可以避免上一次记录的 offset 过大或过小导致下一次恢复位置执行后，
        UI出现大范围跳动的问题
     */
    if (null == view || !view.getGlobalVisibleRect(rect)) {
      lastIdlePositionOffset = 0
      return
    }
    lastIdlePositionOffset = rect.height() - view.height
  }

  private fun restoreLastIdlePositionAndOffset(recyclerView: RecyclerView?) {
    recyclerView ?: return
    if (!markFlag) return
    if (recyclerView.scrollState != RecyclerView.SCROLL_STATE_IDLE) return
    val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return

    if (null != lastRestoreRunnable) recyclerView.removeCallbacks(lastRestoreRunnable)
    lastRestoreRunnable =
      RestoreRunnable(
        layoutManager, lastIdlePosition, lastIdlePositionOffset
      )
    recyclerView.post(lastRestoreRunnable)
  }

  private class RestoreRunnable(
    private val layoutManager: LinearLayoutManager,
    private val position: Int,
    private val offset: Int
  ) : Runnable {

    override fun run() {
      layoutManager.scrollToPositionWithOffset(position, offset)
    }

  }

}