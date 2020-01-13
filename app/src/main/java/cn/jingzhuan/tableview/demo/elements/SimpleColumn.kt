package cn.jingzhuan.tableview.demo.elements

import android.content.Context
import cn.jingzhuan.tableview.element.TextColumn

class SimpleColumn(val source: ColumnData) : TextColumn() {

    override fun getText(context: Context): CharSequence? {
        return source.value
    }

    override fun visible(): Boolean {
        return true
    }

}