package cn.jingzhuan.tableview.fallback

import cn.jingzhuan.tableview.element.Column

internal class EmptyColumn : Column() {

  override fun visible(): Boolean {
    return true
  }

}