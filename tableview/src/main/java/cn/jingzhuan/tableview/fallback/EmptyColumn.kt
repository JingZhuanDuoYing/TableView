package cn.jingzhuan.tableview.fallback

import cn.jingzhuan.tableview.adapter.IRowListAdapterDelegate
import cn.jingzhuan.tableview.element.Column

internal class EmptyColumn : Column() {

  override fun id(): Long {
    return IRowListAdapterDelegate.INVALID_ITEM_ID
  }

  override fun visible(): Boolean {
    return true
  }

}