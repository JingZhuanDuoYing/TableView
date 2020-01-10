package com.nagihong.tableview.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.nagihong.tableview.TableViewLog
import com.nagihong.tableview.fallback.RowListEmptyViewHolder

/**
 * Chenyikang
 * 2018 December 22
 */
class RowListAdapter(private val header: Boolean) : RecyclerView.Adapter<ViewHolder>() {

  var delegate: IRowListDelegate? = null

  init {
    setHasStableIds(true)
  }

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): ViewHolder {
    checkDelegate()
    return delegate?.createViewHolder(parent, viewType, header) ?: RowListEmptyViewHolder(
        parent
    )
  }

  override fun onBindViewHolder(
    holder: ViewHolder,
    position: Int
  ) {
    checkDelegate()
    delegate?.bindViewHolder(holder, position, header)
  }

  override fun getItemCount(): Int {
    checkDelegate()
    return delegate?.getItemCount(header) ?: 0
  }

  override fun getItemId(position: Int): Long {
    checkDelegate()
    return delegate?.getItemId(position, header) ?: IRowListDelegate.INVALID_ITEM_ID
  }

  override fun getItemViewType(position: Int): Int {
    checkDelegate()
    return delegate?.getItemViewType(position, header) ?: IRowListDelegate.INVALID_VIEW_TYPE
  }

  private fun checkDelegate() {
    if (null == delegate) TableViewLog.w(this::class.java.name, "There's not a delegate here")
  }

}