package cn.jingzhuan.tableview.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.jingzhuan.tableview.element.HeaderRow

/**
 * Chenyikang
 * 2018 December 22
 */
interface IRowListAdapterDelegate {

  companion object {
    const val INVALID_VIEW_TYPE = Int.MIN_VALUE
    const val INVALID_ITEM_ID = Long.MIN_VALUE
    const val HEADER_VIEW_TYPE = 110
  }

  var headerRow: HeaderRow<*>?

  fun createViewHolder(
    parent: ViewGroup,
    viewType: Int,
    fromHeader: Boolean
  ): RecyclerView.ViewHolder

  fun bindViewHolder(
    holder: RecyclerView.ViewHolder,
    position: Int,
    fromHeader: Boolean
  )

  fun getItemCount(fromHeader: Boolean): Int

  fun getItemViewType(
    position: Int,
    fromHeader: Boolean
  ): Int

  fun getItemId(
    position: Int,
    fromHeader: Boolean
  ): Long

  fun notifyDataSetChanged()

  fun connect(adapter: RowListAdapter)

}