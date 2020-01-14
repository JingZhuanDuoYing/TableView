package cn.jingzhuan.tableview.element

import android.content.Context
import android.graphics.*
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.text.*
import android.view.Gravity
import android.view.View
import androidx.annotation.ColorInt
import cn.jingzhuan.tableview.sp
import kotlin.math.max

abstract class TextColumn : DrawableColumn() {

    private val drawSimpleTextDirectly = true

    @Transient
    private var drawRegionLeft = 0
    @Transient
    private var drawRegionTop = 0
    @Transient
    private var drawRegionRight = 0
    @Transient
    private var drawRegionBottom = 0

    @Transient
    private var boringLayout: BoringLayout? = null
    @Transient
    private var staticLayout: StaticLayout? = null
    @Transient
    private var dynamicLayout: DynamicLayout? = null
    @Transient
    private var lastMeasuredValue: CharSequence? = null

    private var measuredTextWidth = 0
    private var measuredTextHeight = 0

    open fun typeface(context: Context): Typeface {
        return Typeface.DEFAULT
    }

    open fun textSizeSp(context: Context): Float {
        return 18F
    }

    @ColorInt
    open fun color(context: Context): Int {
        return Color.BLACK
    }

    @ColorInt
    open fun backgroundColor(context: Context): Int? {
        return null
    }

    open fun isBold(context: Context) = false

    abstract fun getText(context: Context): CharSequence?

    override fun measure(context: Context, rowShareElements: RowShareElements) {
        super.measure(context, rowShareElements)
        val textSize = context.sp(textSizeSp(context))
        val color = color(context)
        val typeface = typeface(context)

        val paint = rowShareElements.getPaint(textSize, color, typeface)

        val text = getText(context)
        if (TextUtils.equals(
                text, lastMeasuredValue
            ) && widthWithMargins > 0 && heightWithMargins > 0
        ) return

        when {
            TextUtils.isEmpty(text) -> {
                measuredTextWidth = 0
                measuredTextHeight = 0
            }
            text is Spannable -> {
                measuredTextWidth =
                    DynamicLayout.getDesiredWidth(text, 0, text.length, paint).toInt()
                measuredTextHeight = (paint.descent() - paint.ascent()).toInt()
            }
            else -> {
                measuredTextWidth = paint.measureText(text.toString(), 0, text!!.length).toInt()
                measuredTextHeight = (paint.descent() - paint.ascent()).toInt()
            }
        }

        val margins = margins(context)
        val padding = padding(context)

        val minWidth = max(width(context), minWidth(context))
        val minHeight = max(height(context), minHeight(context))
        val minWidthWithMargins = margins[0] + minWidth + margins[2]
        val minHeightWithMargins = margins[1] + minHeight + margins[3]
        widthWithMargins = margins[0] + padding[0] + measuredTextWidth + padding[2] + margins[2]
        heightWithMargins = margins[1] + padding[1] + measuredTextHeight + padding[3] + margins[3]
        widthWithMargins = max(minWidthWithMargins, widthWithMargins)
        heightWithMargins = max(minHeightWithMargins, heightWithMargins)

        lastMeasuredValue = text
    }

    override fun prepareToDraw(context: Context, rowShareElements: RowShareElements) {
        super.prepareToDraw(context, rowShareElements)

        val textSize = context.sp(textSizeSp(context))
        val color = color(context)
        val typeface = typeface(context)

        val paint = rowShareElements.getPaint(textSize, color, typeface)

        val text = getText(context)

        if (!TextUtils.isEmpty(text) && text is String && !drawSimpleTextDirectly) {
            if(null != boringLayout && text == boringLayout!!.text) return
            // before lollipop, BoringLayout.isBoring may return null
            val params =
                BoringLayout.isBoring(text, paint)
                    ?: BoringLayout.Metrics().apply {
                        top = paint.fontMetricsInt.top
                        ascent = paint.fontMetricsInt.ascent
                        descent = paint.fontMetricsInt.descent
                        bottom = paint.fontMetricsInt.bottom
                        leading = paint.fontMetricsInt.leading
                        this.width = measuredTextWidth
                    }
            // BoringLayout may not working on some devices when meet chinese words
            if (null == boringLayout) {
                boringLayout = BoringLayout.make(
                    text,
                    paint,
                    measuredTextWidth,
                    Layout.Alignment.ALIGN_NORMAL,
                    0F,
                    0F,
                    params,
                    true,
                    null,
                    measuredTextWidth
                )
            } else {
                boringLayout!!.replaceOrMake(
                    text,
                    paint,
                    measuredTextWidth,
                    Layout.Alignment.ALIGN_NORMAL,
                    0F,
                    0F,
                    params,
                    true,
                    null,
                    measuredTextWidth
                )
            }
        } else if (!TextUtils.isEmpty(text) && text is Spannable) {
            if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
                if(null != staticLayout && text == staticLayout!!.text) return
                staticLayout = if (VERSION.SDK_INT >= VERSION_CODES.M) {
                    StaticLayout.Builder.obtain(
                        text,
                        0,
                        text.length,
                        paint,
                        Int.MAX_VALUE
                    )
                        .build()
                } else {
                    StaticLayout(
                        text,
                        paint,
                        Int.MAX_VALUE,
                        Layout.Alignment.ALIGN_NORMAL,
                        0F,
                        0F,
                        true
                    )
                }
            } else {
                if(null != dynamicLayout && text == dynamicLayout!!.text) return
                dynamicLayout = DynamicLayout(
                    text,
                    paint,
                    Int.MAX_VALUE,
                    Layout.Alignment.ALIGN_NORMAL,
                    0F,
                    0F,
                    true
                )
            }
        }
    }

    override fun layout(
        context: Context,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        rowShareElements: RowShareElements
    ) {
        super.layout(context, left, top, right, bottom, rowShareElements)
        val margins = margins(context)
        val padding = padding(context)

        val contentLeft = left + margins[0] + padding[0]
        val contentTop = top + margins[1] + padding[1]
        val contentRight = right - margins[2] - padding[2]
        val contentBottom = bottom - margins[3] - padding[3]
        val rectLayout = rowShareElements.rect1
        rectLayout.set(contentLeft, contentTop, contentRight, contentBottom)

        val gravity = gravity()
        val rectDraw = rowShareElements.rect2
        Gravity.apply(gravity, measuredTextWidth, measuredTextHeight, rectLayout, rectDraw)
        drawRegionLeft = rectDraw.left
        drawRegionTop = rectDraw.top
        drawRegionRight = rectDraw.right
        drawRegionBottom = rectDraw.bottom
    }

    override fun shouldIgnoreDraw(container: View): Boolean {
        val left = container.scrollX
        val right = left + container.width
        val top = container.scrollY
        val bottom = top + container.height
        return drawRegionRight < left || drawRegionLeft > right || drawRegionBottom < top || drawRegionTop > bottom
    }

    override fun draw(
        context: Context,
        canvas: Canvas,
        rowShareElements: RowShareElements
    ) {
        super.draw(context, canvas, rowShareElements)

        val textSize = context.sp(textSizeSp(context))
        val color = color(context)
        val typeface = typeface(context)
        val paint = rowShareElements.getPaint(textSize, color, typeface)
        val text = getText(context) ?: ""

        val drawLeft = drawRegionLeft.toFloat()
        val drawTop = drawRegionTop.toFloat()
        val drawRegionHeight = drawRegionBottom - drawRegionTop
        canvas.save()
        if (text is String && null != boringLayout) {
            if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
                canvas.translate(drawLeft, drawTop)
            } else {
                canvas.translate(drawLeft, drawTop - paint.descent())
            }
            boringLayout?.draw(canvas)
        } else if (text is Spannable) {
            if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
                if (null != staticLayout) {
                    canvas.translate(drawLeft, drawTop - paint.descent())
                    staticLayout?.draw(canvas)
                } else {
                    canvas.drawText(
                        text,
                        0,
                        text.length,
                        drawLeft,
                        drawTop + drawRegionHeight,
                        paint
                    )
                }
            } else {
                if (null != dynamicLayout) {
                    canvas.translate(drawLeft, drawTop - paint.descent())
                    dynamicLayout?.draw(canvas)
                } else {
                    canvas.drawText(
                        text,
                        0,
                        text.length,
                        drawLeft,
                        drawTop + drawRegionHeight,
                        paint
                    )
                }
            }
        } else {
            canvas.drawText(text.toString(), drawLeft, drawTop + drawRegionHeight, paint)
        }
        canvas.restore()
    }

}