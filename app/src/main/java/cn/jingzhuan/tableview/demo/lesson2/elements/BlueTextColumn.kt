package cn.jingzhuan.tableview.demo.lesson2.elements

import android.content.Context
import android.graphics.Color
import cn.jingzhuan.tableview.element.TextColumn

class BlueTextColumn(private val value: String) : TextColumn() {

    override fun getText(context: Context): CharSequence? {
        return value
    }

    override fun color(context: Context): Int {
        return Color.BLUE
    }

}