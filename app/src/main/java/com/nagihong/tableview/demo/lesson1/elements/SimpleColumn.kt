package com.nagihong.tableview.demo.lesson1.elements

import android.content.Context
import com.nagihong.tableview.element.TextColumn

class SimpleColumn(val source: ColumnData) : TextColumn() {

    override fun getText(context: Context): CharSequence? {
        return source.value
    }

    override fun visible(): Boolean {
        return true
    }

}