package cn.jingzhuan.tableview.listeners

import android.content.Context
import android.view.View
import cn.jingzhuan.tableview.element.Column

interface OnRowClickListener {

    fun onRowClick(
        context: Context,
        rowLayout: View,
        columnView: View? = null,
        column: Column,
        sticky: Boolean,
        x: Int,
        y: Int
    )

}