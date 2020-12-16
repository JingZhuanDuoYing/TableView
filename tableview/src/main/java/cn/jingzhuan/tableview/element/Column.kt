package cn.jingzhuan.tableview.element

import android.content.Context
import android.support.annotation.Px
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import cn.jingzhuan.tableview.annotations.DP
import cn.jingzhuan.tableview.dp
import cn.jingzhuan.tableview.listeners.OnColumnClickListener
import cn.jingzhuan.tableview.listeners.OnColumnLongClickListener

/**
 * Chenyikang
 * 2018 December 21
 */
abstract class Column : IElement, OnColumnClickListener, OnColumnLongClickListener {

    override var debugUI: Boolean = false

    // <editor-fold desc="public">    ----------------------------------------------------------

    @DP
    var minWidth: Int = 0

    @DP
    var minHeight: Int = 0

    @DP
    var width: Int = ViewGroup.LayoutParams.WRAP_CONTENT

    @DP
    var height: Int = ViewGroup.LayoutParams.MATCH_PARENT

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

    // </editor-fold desc="public">    --------------------------------------------------------

    // <editor-fold desc="internal">    ----------------------------------------------------------

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

    // </editor-fold desc="internal">    ---------------------------------------------------------

    constructor()

    constructor(
        @DP minWidth: Int? = null,
        @DP minHeight: Int? = null,
        @DP width: Int? = null,
        @DP height: Int? = null,
        @Px leftMargin: Int? = null,
        @Px topMargin: Int? = null,
        @Px rightMargin: Int? = null,
        @Px bottomMargin: Int? = null,
        @Px paddingLeft: Int? = null,
        @Px paddingTop: Int? = null,
        @Px paddingRight: Int? = null,
        @Px paddingBottom: Int? = null,
        gravity: Int? = null,
        visible: Boolean? = null
    ) : this() {
        this.minWidth = minWidth ?: this.minWidth
        this.minHeight = minHeight ?: this.minHeight
        this.width = width ?: this.width
        this.height = height ?: this.height
        this.leftMargin = leftMargin ?: this.leftMargin
        this.topMargin = topMargin ?: this.topMargin
        this.rightMargin = rightMargin ?: this.rightMargin
        this.bottomMargin = bottomMargin ?: this.bottomMargin
        this.paddingLeft = paddingLeft ?: this.paddingLeft
        this.paddingTop = paddingTop ?: this.paddingTop
        this.paddingRight = paddingRight ?: this.paddingRight
        this.paddingBottom = paddingBottom ?: this.paddingBottom
        this.gravity = gravity ?: this.gravity
        this.visible = visible ?: this.visible
    }

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

    override fun width() = width

    override fun minWidth() = minWidth

    override fun height() = height

    override fun minHeight() = minHeight

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

    override fun onClick(
        context: Context,
        rowLayout: View,
        columnView: View?,
        row: Row<Column>,
        column: Column
    ) {

    }

    override fun onLongClick(
        context: Context,
        rowLayout: View,
        columnView: View?,
        row: Row<Column>,
        column: Column
    ) {

    }

}