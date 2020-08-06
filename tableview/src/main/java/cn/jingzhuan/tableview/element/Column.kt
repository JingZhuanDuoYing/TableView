package cn.jingzhuan.tableview.element

import android.content.Context
import android.support.annotation.Dimension
import android.support.annotation.Px
import android.view.Gravity
import android.view.ViewGroup
import cn.jingzhuan.tableview.dp

/**
 * Chenyikang
 * 2018 December 21
 */
abstract class Column : IElement {

    override var debugUI: Boolean = false

    @Dimension(unit = Dimension.DP)
    override var minWidth: Int = 0

    @Dimension(unit = Dimension.DP)
    override var minHeight: Int = 0

    @Dimension(unit = Dimension.DP)
    override var width: Int = ViewGroup.LayoutParams.WRAP_CONTENT

    @Dimension(unit = Dimension.DP)
    override var height: Int = ViewGroup.LayoutParams.MATCH_PARENT

    @Px
    var widthWithMargins = 0
        internal set
    @Px
    var heightWithMargins = 0
        internal set

    @Px
    var columnLeft = 0
        internal set
    @Px
    var columnTop = 0
        internal set
    @Px
    var columnRight = 0
        internal set
    @Px
    var columnBottom = 0
        internal set

    @Px
    var leftMargin = 0
    @Px
    var topMargin = 0
    @Px
    var rightMargin = 0
    @Px
    var bottomMargin = 0

    @Px
    var paddingLeft = 0
    @Px
    var paddingTop = 0
    @Px
    var paddingRight = 0
    @Px
    var paddingBottom = 0

    var gravity: Int = Gravity.END or Gravity.CENTER_VERTICAL

    var weight = 1

    var visible = true

    @Deprecated("20200806 useless variable field")
    internal var laidOut = false

    @Deprecated("20200806 use variable field instead", ReplaceWith(""))
    open fun minWidth(context: Context): Int {
        return context.dp(minWidth).toInt()
    }

    @Deprecated("20200806 use variable field instead", ReplaceWith(""))
    open fun minHeight(context: Context): Int {
        return context.dp(minHeight).toInt()
    }

    @Deprecated("20200806 use variable field instead", ReplaceWith(""))
    override fun height(context: Context): Int {
        return if (height <= 0) height else context.dp(height).toInt()
    }

    @Deprecated("20200806 use variable field instead", ReplaceWith(""))
    override fun width(context: Context): Int {
        return if (width <= 0) width else context.dp(width).toInt()
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