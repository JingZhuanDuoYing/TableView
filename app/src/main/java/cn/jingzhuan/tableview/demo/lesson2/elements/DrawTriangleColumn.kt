package cn.jingzhuan.tableview.demo.lesson2.elements

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import cn.jingzhuan.tableview.element.DrawableColumn
import cn.jingzhuan.tableview.element.RowShareElements

class DrawTriangleColumn : DrawableColumn() {

    private val path = Path()
    private var lined = false
    private val paint = Paint()

    init {
        height = 70
        width = 100
    }

    override fun prepareToDraw(context: Context, rowShareElements: RowShareElements) {
        super.prepareToDraw(context, rowShareElements)

        if (lined) return

        paint.color = Color.MAGENTA
        paint.isDither = true
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL

        val triangleRightMargin = 20
        val triangleRight = columnRight - triangleRightMargin
        val triangleLeft = triangleRight - 70
        val centerX = triangleRight - 70 / 2
        val triangleTop = (columnBottom - 70) / 2
        val triangleBottom = triangleTop + 70
        path.moveTo(centerX.toFloat(), triangleTop.toFloat())
        path.lineTo(triangleRight.toFloat(), triangleBottom.toFloat())
        path.lineTo(triangleLeft.toFloat(), triangleBottom.toFloat())
        path.lineTo(centerX.toFloat(), triangleTop.toFloat())
        path.close()

        lined = true
    }

    override fun draw(context: Context, canvas: Canvas, rowShareElements: RowShareElements) {
        super.draw(context, canvas, rowShareElements)
        canvas.drawPath(path, paint)
    }

}