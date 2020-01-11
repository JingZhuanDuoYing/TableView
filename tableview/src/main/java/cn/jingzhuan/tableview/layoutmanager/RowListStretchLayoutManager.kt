package cn.jingzhuan.tableview.layoutmanager

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.jingzhuan.tableview.RowLayout

/**
 * Chenyikang
 * 2019 January 10
 */
internal class RowListStretchLayoutManager(
  context: Context,
  orientation: Int = RecyclerView.VERTICAL,
  reverseLayout: Boolean = false
) : LinearLayoutManager(context, orientation, reverseLayout) {

  override fun layoutDecorated(
    child: View,
    left: Int,
    top: Int,
    right: Int,
    bottom: Int
  ) {
    super.layoutDecorated(child, left, top, right, bottom)
    layout(child)
  }

  override fun layoutDecoratedWithMargins(
    child: View,
    left: Int,
    top: Int,
    right: Int,
    bottom: Int
  ) {
    super.layoutDecoratedWithMargins(child, left, top, right, bottom)
    layout(child)
  }

  private fun layout(child: View) {
    if (child is RowLayout) child.relayoutInStretchMode()
  }

}