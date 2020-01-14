package cn.jingzhuan.tableview.element

import android.content.Context
import android.graphics.*
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.text.BoringLayout
import android.text.Layout
import android.text.Spannable
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import androidx.annotation.ColorInt
import cn.jingzhuan.tableview.dp
import cn.jingzhuan.tableview.sp
import kotlin.math.max

abstract class TextColumn : DrawableColumn() {

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

        if (TextUtils.isEmpty(text)) {
            measuredTextWidth = 0
            measuredTextHeight = 0
        } else {
            val rectLayout = rowShareElements.rect1
            paint.getTextBounds(text.toString(), 0, text!!.length, rectLayout)
            measuredTextWidth = max(rectLayout.width(), 0)
            measuredTextHeight = rectLayout.height()
        }

        val margins = margins(context)
        val padding = padding(context)

        if (!TextUtils.isEmpty(text) && text is String) {
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
            boringLayout?.height?.apply {
                // before lollipop, this may be zero
                measuredTextHeight = max(measuredTextHeight, this)
            }
        } else if (!TextUtils.isEmpty(text) && text is Spannable) {
            staticLayout = when {
                VERSION.SDK_INT >= VERSION_CODES.M -> {
                    StaticLayout.Builder.obtain(
                        text,
                        0,
                        text.length,
                        paint,
                        Int.MAX_VALUE
                    )
                        .build()
                }
                VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP -> {
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
                else -> null
            }
            measuredTextWidth = if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
                max(staticLayout!!.getLineWidth(0).toInt(), 0)
            } else {
                StaticLayout.getDesiredWidth(text, 0, text.length, paint).toInt()
            }
        }

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
        } else if (text is Spannable && null != staticLayout) {
            if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
                canvas.translate(drawLeft, drawTop - paint.descent())
                staticLayout?.draw(canvas)
            } else {
                canvas.drawText(text, 0, text.length, drawLeft, drawTop + drawRegionHeight, paint)
            }
        } else {
            canvas.drawText(text.toString(), drawLeft, drawTop + drawRegionHeight, paint)
        }
        canvas.restore()
    }

}