package cn.jingzhuan.tableview.demo.lesson2.elements

import android.content.Context
import cn.jingzhuan.tableview.demo.dp
import cn.jingzhuan.tableview.element.TextColumn

class FixedHeightColumn(private val value: String, private val fixedHeightDp: Float) :
    TextColumn() {

    override fun height(context: Context): Int {
        return context.dp(fixedHeightDp).toInt()
    }

    override fun getText(context: Context): CharSequence? {
        return value
    }

}