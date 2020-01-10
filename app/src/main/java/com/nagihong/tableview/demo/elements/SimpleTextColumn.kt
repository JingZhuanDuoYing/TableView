package com.nagihong.tableview.demo.elements

import com.nagihong.tableview.element.Column

class SimpleTextColumn(val source: ColumnData) : Column() {

    override fun id(): Long {
        return source.id.hashCode().toLong()
    }

    override fun visible(): Boolean {
        return true
    }

}