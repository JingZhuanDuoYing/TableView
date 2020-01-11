package cn.jingzhuan.tableview.demo.lesson1.elements

import cn.jingzhuan.tableview.element.Column
import cn.jingzhuan.tableview.element.Row

class SimpleRow(val source: RowData, columns: List<Column>) : Row<Column>(columns) {

    override fun type(): Int {
        return 2
    }

    override fun id(): Long {
        return source.id.hashCode().toLong()
    }

    override fun visible(): Boolean {
        return true
    }

}