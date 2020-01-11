package cn.jingzhuan.tableview.layoutmanager

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.Recycler
import androidx.recyclerview.widget.RecyclerView.State

/**
 * Chenyikang
 * 7/24/18
 */
internal class RowListLayoutManager(
  context: Context,
  private val scrollHorizontallyBy: (dx: Int, remainingScrollHorizontal: Int) -> Int
) : LinearLayoutManager(context) {

  override fun canScrollVertically() = true

  override fun canScrollHorizontally() = true

  override fun scrollHorizontallyBy(
    dx: Int,
    recycler: Recycler,
    state: State
  ) = scrollHorizontallyBy.invoke(dx, state.remainingScrollHorizontal)
}