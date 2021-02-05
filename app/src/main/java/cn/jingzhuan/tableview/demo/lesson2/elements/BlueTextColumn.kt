package cn.jingzhuan.tableview.demo.lesson2.elements

import android.content.Context
import android.graphics.Color
import cn.jingzhuan.tableview.element.TextColumn

class BlueTextColumn(private val value: String) : TextColumn() {

    init {
        color = Color.BLUE
    }

    override fun getText(context: Context): CharSequence? {
        return value
    }
}