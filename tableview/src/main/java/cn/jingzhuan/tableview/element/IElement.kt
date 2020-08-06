package cn.jingzhuan.tableview.element

import android.content.Context
import java.io.Serializable

/**
 * Chenyikang
 * 2018 December 21
 */
interface IElement : Serializable {

  /**
   * dp
   */
  var width: Int

  /**
   * dp
   */
  var height: Int

  /**
   * dp
   */
  var minWidth: Int

  /**
   * dp
   */
  var minHeight: Int

  var debugUI: Boolean

  @Deprecated("20200806 use variable field instead")
  fun height(context: Context): Int

  @Deprecated("20200806 use variable field instead")
  fun width(context: Context): Int

  @Deprecated("20200806 use variable field instead", ReplaceWith(""))
  fun debugUI() = debugUI

}