package cn.jingzhuan.tableview.demo.elements

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.Gravity
import cn.jingzhuan.tableview.demo.dp
import cn.jingzhuan.tableview.element.RowShareElements
import cn.jingzhuan.tableview.element.TextColumn

class SimpleHeaderColumn(private val value: String) : TextColumn() {

    init {
        gravity = Gravity.CENTER
        paddingRight = 20
        backgroundColor = Color.parseColor("#D4D4D4")
    }

    override fun getText(context: Context): CharSequence? {
        return value
    }

}