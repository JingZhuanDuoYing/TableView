package cn.jingzhuan.tableview.demo.lesson2.elements

import android.content.Context
import cn.jingzhuan.tableview.demo.dp
import cn.jingzhuan.tableview.element.TextColumn

class FixedWidthColumn(private val value: String, private val fixedWidthDp: Float) : TextColumn() {

    init {
        width = fixedWidthDp.toInt()
    }

    override fun getText(context: Context): CharSequence? {
        return value
    }

}