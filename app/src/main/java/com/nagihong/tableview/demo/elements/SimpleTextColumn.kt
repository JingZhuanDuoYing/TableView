package com.nagihong.tableview.demo.elements

import android.content.Context
import com.nagihong.tableview.element.TextColumn

class SimpleTextColumn(val source: ColumnData) : TextColumn() {

    override fun getText(context: Context): CharSequence? {
        return source.value
    }

    override fun visible(): Boolean {
        return true
    }

}