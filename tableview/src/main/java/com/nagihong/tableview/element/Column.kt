package com.nagihong.tableview.element

import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import com.nagihong.tableview.dp

/**
 * Chenyikang
 * 2018 December 21
 */
abstract class Column : IElement {

    var widthWithMargins = 0
    var heightWithMargins = 0

    var left = 0
    var top = 0
    var right = 0
    var bottom = 0

    var columnLeft = 0
    var columnTop = 0
    var columnRight = 0
    var columnBottom = 0

    var laidOut = false

    override fun id() = Long.MAX_VALUE

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

    open fun gravity(): Int {
        return Gravity.END or Gravity.CENTER_VERTICAL
    }

    open fun margins(context: Context): Array<Int> {
        return arrayOf(0, 0, 0, 0)
    }

    open fun padding(context: Context): Array<Int> {
        return arrayOf(context.dp(15F).toInt(), 0, context.dp(10F).toInt(), 0)
    }

    open fun layout(
        context: Context,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        this.left = left
        this.top = top
        this.right = right
        this.bottom = bottom
        laidOut = true
    }

}