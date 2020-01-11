package com.nagihong.tableview.demo.lesson1.elements

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.nagihong.tableview.element.Column
import com.nagihong.tableview.element.Row

class TitleRow(columns: List<Column>): Row<Column>(columns) {

    private val backgroundPaint = Paint().apply {
        isAntiAlias = true
        isDither = true
        style = Paint.Style.FILL
        color = Color.parseColor("#B1B3B3")
    }

    override fun type(): Int {
        return 1
    }

    override fun id(): Long {
        return Long.MAX_VALUE
    }

    override fun visible(): Boolean {
        return true
    }

    override fun draw(context: Context, canvas: Canvas, stickyWidthWithMargins: Int) {
        super.draw(context, canvas, stickyWidthWithMargins)
        canvas.drawRect(0F, 0F, canvas.width.toFloat(), canvas.height.toFloat(), backgroundPaint)
    }

}