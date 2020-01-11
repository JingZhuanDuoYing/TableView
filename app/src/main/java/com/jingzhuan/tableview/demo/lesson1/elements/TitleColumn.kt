package cn.jingzhuan.tableview.demo.lesson1.elements

import android.content.Context
import cn.jingzhuan.tableview.element.TextColumn

class TitleColumn(val index: Int) : TextColumn() {

    override fun getText(context: Context): CharSequence? {
        return "Column $index"
    }

    override fun visible(): Boolean {
        return true
    }
}