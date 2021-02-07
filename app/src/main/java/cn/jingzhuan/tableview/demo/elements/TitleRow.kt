package cn.jingzhuan.tableview.demo.elements

import android.content.Context
import android.graphics.Color
import android.view.View
import cn.jingzhuan.tableview.element.Column
import cn.jingzhuan.tableview.element.HeaderRow
import timber.log.Timber

class TitleRow(columns: List<Column>) : HeaderRow<Column>(columns) {

    init {
        backgroundColor = Color.parseColor("#B1B3B3")
    }

    override fun onRowClick(
        context: Context,
        rowLayout: View,
        columnView: View?,
        column: Column,
        sticky: Boolean,
        x: Int,
        y: Int
    ) {
        super.onRowClick(context, rowLayout, columnView, column, sticky, x, y)
        Timber.d("onClick() index: ${columns.indexOf(column)}")
    }

    override fun onRowLongClick(
        context: Context,
        rowLayout: View,
        columnView: View?,
        column: Column,
        sticky: Boolean,
        x: Int,
        y: Int
    ) {
        super.onRowLongClick(context, rowLayout, columnView, column, sticky, x, y)
        Timber.d("onLongClick() index: ${columns.indexOf(column)}")
    }

}