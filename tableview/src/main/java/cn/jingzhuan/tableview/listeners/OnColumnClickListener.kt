package cn.jingzhuan.tableview.listeners

import android.content.Context
import android.view.View
import cn.jingzhuan.tableview.element.Column
import cn.jingzhuan.tableview.element.Row

interface OnColumnClickListener {

    fun onColumnClick(
        context: Context,
        rowLayout: View,
        columnView: View? = null,
        row: Row<Column>,
        column: Column
    )

}