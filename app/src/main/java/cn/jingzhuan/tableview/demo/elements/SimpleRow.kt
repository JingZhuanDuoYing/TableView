package cn.jingzhuan.tableview.demo.elements

import cn.jingzhuan.tableview.element.Column
import cn.jingzhuan.tableview.element.Row

class SimpleRow(columns: List<Column>, private val type: Int = 999) : Row<Column>(columns) {

    override fun type(): Int {
        return type
    }

}