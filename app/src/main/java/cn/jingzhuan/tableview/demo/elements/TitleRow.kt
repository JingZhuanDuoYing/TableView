package cn.jingzhuan.tableview.demo.elements

import android.content.Context
import android.graphics.Color
import android.view.View
import cn.jingzhuan.tableview.element.Column
import cn.jingzhuan.tableview.element.HeaderRow
import timber.log.Timber

class TitleRow(columns: List<Column>) : HeaderRow<Column>(columns) {

    override fun backgroundColor(context: Context): Int? {
        return Color.parseColor("#B1B3B3")
    }

    override fun onClick(
        context: Context,
        rowLayout: View,
        columnView: View?,
        column: Column,
        sticky: Boolean,
        x: Int,
        y: Int
    ) {
        super.onClick(context, rowLayout, columnView, column, sticky, x, y)
        Timber.d("onClick() index: ${columns.indexOf(column)}")
    }

    override fun onLongClick(
        context: Context,
        rowLayout: View,
        columnView: View?,
        column: Column,
        sticky: Boolean,
        x: Int,
        y: Int
    ) {
        super.onLongClick(context, rowLayout, columnView, column, sticky, x, y)
        Timber.d("onLongClick() index: ${columns.indexOf(column)}")
    }

}