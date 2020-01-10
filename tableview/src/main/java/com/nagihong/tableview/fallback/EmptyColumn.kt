package com.nagihong.tableview.fallback

import com.nagihong.tableview.adapter.IRowListAdapterDelegate
import com.nagihong.tableview.element.Column

internal class EmptyColumn : Column() {

  override fun id(): Long {
    return IRowListAdapterDelegate.INVALID_ITEM_ID
  }

  override fun visible(): Boolean {
    return true
  }

}