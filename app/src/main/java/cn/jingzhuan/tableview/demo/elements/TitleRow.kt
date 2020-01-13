package cn.jingzhuan.tableview.demo.elements

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import cn.jingzhuan.tableview.element.Column
import cn.jingzhuan.tableview.element.HeaderRow

class TitleRow(columns: List<Column>): HeaderRow<Column>(columns) {

    private val backgroundPaint = Paint().apply {
        isAntiAlias = true
        isDither = true
        style = Paint.Style.FILL
        color = Color.parseColor("#B1B3B3")
    }

    override fun visible(): Boolean {
        return true
    }

    override fun draw(context: Context, canvas: Canvas, stickyWidthWithMargins: Int) {
        super.draw(context, canvas, stickyWidthWithMargins)
        canvas.drawRect(0F, 0F, canvas.width.toFloat(), canvas.height.toFloat(), backgroundPaint)
    }

}