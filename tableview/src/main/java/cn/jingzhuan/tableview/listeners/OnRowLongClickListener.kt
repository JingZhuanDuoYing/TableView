package cn.jingzhuan.tableview.listeners

import android.content.Context
import android.view.View
import cn.jingzhuan.tableview.element.Column

interface OnRowLongClickListener {

    fun onLongClick(
        context: Context,
        rowLayout: View,
        columnView: View? = null,
        column: Column,
        sticky: Boolean,
        x: Int,
        y: Int
    )

}