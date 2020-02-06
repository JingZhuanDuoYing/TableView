package cn.jingzhuan.tableview.adapter

import android.support.v7.widget.RecyclerView

internal class AdapterDataObserver(private val onChanged: (() -> Unit)) : RecyclerView.AdapterDataObserver() {

  private fun callback() {
    onChanged.invoke()
  }

  override fun onChanged() {
    super.onChanged()
    callback()
  }

  override fun onItemRangeRemoved(
    positionStart: Int,
    itemCount: Int
  ) {
    super.onItemRangeRemoved(positionStart, itemCount)
    callback()
  }

  override fun onItemRangeMoved(
    fromPosition: Int,
    toPosition: Int,
    itemCount: Int
  ) {
    super.onItemRangeMoved(fromPosition, toPosition, itemCount)
    callback()
  }

  override fun onItemRangeInserted(
    positionStart: Int,
    itemCount: Int
  ) {
    super.onItemRangeInserted(positionStart, itemCount)
    callback()
  }

  override fun onItemRangeChanged(
    positionStart: Int,
    itemCount: Int
  ) {
    super.onItemRangeChanged(positionStart, itemCount)
    callback()
  }

  override fun onItemRangeChanged(
    positionStart: Int,
    itemCount: Int,
    payload: Any?
  ) {
    super.onItemRangeChanged(positionStart, itemCount, payload)
    callback()
  }

}