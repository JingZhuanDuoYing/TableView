package cn.jingzhuan.tableview.element

import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import cn.jingzhuan.tableview.dp

/**
 * Chenyikang
 * 2018 December 21
 */
abstract class Column : IElement {

    var widthWithMargins = 0
        internal set
    var heightWithMargins = 0
        internal set

    var columnLeft = 0
        internal set
    var columnTop = 0
        internal set
    var columnRight = 0
        internal set
    var columnBottom = 0
        internal set

    var leftMargin = 0
    var topMargin = 0
    var rightMargin = 0
    var bottomMargin = 0

    var paddingLeft = 0
    var paddingTop = 0
    var paddingRight = 0
    var paddingBottom = 0

    var gravity: Int = Gravity.END or Gravity.CENTER_VERTICAL

    internal var laidOut = false

    open fun minWidth(context: Context): Int {
        return context.dp(90F).toInt()
    }

    open fun minHeight(context: Context): Int {
        return 0
    }

    override fun height(context: Context): Int {
        return ViewGroup.LayoutParams.MATCH_PARENT
    }

    override fun width(context: Context): Int {
        return ViewGroup.LayoutParams.WRAP_CONTENT
    }

    internal open fun layout(
        context: Context,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        rowShareElements: RowShareElements
    ) {
        laidOut = true
    }

}