package cn.jingzhuan.tableview.element

import android.content.Context
import java.io.Serializable

/**
 * Chenyikang
 * 2018 December 21
 */
interface IElement : Serializable {

  fun visible(): Boolean

  fun height(context: Context): Int

  fun width(context: Context): Int

  fun debugUI() = false

}