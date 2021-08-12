package cn.jingzhuan.tableview.expandable

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup

/**
 * Chenyikang
 * 2018 August 29
 */
internal abstract class IColumnListChildAdapter<VH: RecyclerView.ViewHolder> {

  protected val defaultStickyChildViewType = -4
  protected val defaultChildViewType = -5

  abstract fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH

  abstract fun onBindViewHolder(viewHolder: VH, position: Int, childPosition: Int)

  abstract fun onGetViewType(position: Int, childPosition: Int): Int

  abstract fun onGetItemCountAt(position: Int): Int

  open fun onGetItemId(position: Int, childPosition: Int) = 0L

  abstract fun onItemClick(view: View, viewHolder: VH, position: Int, childPosition: Int)

  abstract fun onToggleExpandStatus(position: Int)

}