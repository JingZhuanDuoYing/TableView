package cn.jingzhuan.tableview.demo.elements

import cn.jingzhuan.tableview.element.Column
import cn.jingzhuan.tableview.element.Row

class SimpleRow(val source: RowData, columns: List<Column>) : Row<Column>(columns) {

    override fun type(): Int {
        return 2
    }

    override fun visible(): Boolean {
        return true
    }

}