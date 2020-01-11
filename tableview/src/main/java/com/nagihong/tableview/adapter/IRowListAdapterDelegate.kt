package com.nagihong.tableview.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.nagihong.tableview.element.Row
import com.nagihong.tableview.layoutmanager.ColumnsLayoutManager

/**
 * Chenyikang
 * 2018 December 22
 */
interface IRowListAdapterDelegate {

  companion object {
    const val INVALID_VIEW_TYPE = Int.MIN_VALUE
    const val INVALID_ITEM_ID = Long.MIN_VALUE
  }

  var columnsLayoutManager: ColumnsLayoutManager?

  fun setTitleRow(row: Row<*>)

  fun setRows(rows: List<Row<*>>)

  fun setStickyRows(rows: List<Row<*>>)

  fun createViewHolder(
    parent: ViewGroup,
    viewType: Int,
    fromHeader: Boolean
  ): ViewHolder

  fun bindViewHolder(
    holder: ViewHolder,
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