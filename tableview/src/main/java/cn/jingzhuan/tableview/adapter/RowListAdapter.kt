package cn.jingzhuan.tableview.adapter

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import cn.jingzhuan.tableview.TableViewLog
import cn.jingzhuan.tableview.fallback.RowListEmptyViewHolder

/**
 * Chenyikang
 * 2018 December 22
 */
class RowListAdapter(private val header: Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  var delegate: IRowListAdapterDelegate? = null

  init {
    setHasStableIds(true)
  }

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): RecyclerView.ViewHolder {
    checkDelegate()
    return delegate?.createViewHolder(parent, viewType, header) ?: RowListEmptyViewHolder(
        parent
    )
  }

  override fun onBindViewHolder(
    holder: RecyclerView.ViewHolder,
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
    return delegate?.getItemId(position, header) ?: IRowListAdapterDelegate.INVALID_ITEM_ID
  }

  override fun getItemViewType(position: Int): Int {
    checkDelegate()
    return delegate?.getItemViewType(position, header) ?: IRowListAdapterDelegate.INVALID_VIEW_TYPE
  }

  private fun checkDelegate() {
    if (null == delegate) TableViewLog.w(this::class.java.name, "There's not a delegate here")
  }

}