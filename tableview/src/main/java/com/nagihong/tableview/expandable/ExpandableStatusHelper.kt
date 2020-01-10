package com.nagihong.tableview.expandable

/**
 * Chenyikang
 * 2018 August 30
 */
internal class ExpandableStatusHelper {

  private val stickyExpandStatus = mutableMapOf<Int, Boolean>()
  private val expandStatus = mutableMapOf<Int, Boolean>()
  var multiExpandable = false

  fun toggleSticky(position: Int): Boolean {
    val expanded = stickyExpandStatus[position] ?: false
    if (!multiExpandable) {
      stickyExpandStatus.clear()
    }
    stickyExpandStatus[position] = !expanded
    return stickyExpandStatus[position] ?: false
  }

  fun toggle(position: Int): Boolean {
    val expanded = expandStatus[position] ?: false
    if (!multiExpandable) {
      expandStatus.clear()
    }
    expandStatus[position] = !expanded
    return expandStatus[position] ?: false
  }

  fun isStickyExpanded(position: Int) = stickyExpandStatus[position] ?: false

  fun isExpanded(position: Int) = expandStatus[position] ?: false

  fun clear() {
    stickyExpandStatus.clear()
    expandStatus.clear()
  }

}