package cn.jingzhuan.tableview.demo.elements

import android.content.Context
import android.graphics.Color
import cn.jingzhuan.tableview.element.Column
import cn.jingzhuan.tableview.element.HeaderRow

class TitleRow(columns: List<Column>) : HeaderRow<Column>(columns) {

    override fun backgroundColor(context: Context): Int? {
        return Color.parseColor("#B1B3B3")
    }

}