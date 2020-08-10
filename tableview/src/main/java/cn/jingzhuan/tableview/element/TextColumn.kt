package cn.jingzhuan.tableview.element

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.support.annotation.ColorInt
import android.text.*
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import cn.jingzhuan.tableview.dp
import cn.jingzhuan.tableview.sp
import java.io.ObjectInputStream
import kotlin.math.max

abstract class TextColumn : DrawableColumn() {

    private val drawSimpleTextDirectly = true

    init {
        paddingLeft = 30
        paddingRight = 30
    }

    @Transient
    var drawRegionLeft = 0
        private set

    @Transient
    var drawRegionTop = 0
        private set

    @Transient
    var drawRegionRight = 0
        private set

    @Transient
    var drawRegionBottom = 0
        private set

    @Transient
    private var boringLayout: BoringLayout? = null

    @Transient
    private var staticLayout: StaticLayout? = null

    @Transient
    private var dynamicLayout: DynamicLayout? = null

    @Transient
    private var lastMeasuredValue: CharSequence? = null

    var measuredTextWidth = 0
        private set
    var measuredTextHeight = 0
        private set

    open var typeface: Typeface = Typeface.DEFAULT
    var textSize: Float = 18F
    open var textSizeUnit: Int = TypedValue.COMPLEX_UNIT_SP

    @ColorInt
    var textColor: Int = Color.BLACK

    var backgroundColor: Int? = null

    private fun readObject(inputStream: ObjectInputStream) {
        inputStream.defaultReadObject()
        drawRegionLeft = 0
        drawRegionTop = 0
        drawRegionRight = 0
        drawRegionBottom = 0
    }

    @Deprecated("20200806 use variable field instead", ReplaceWith("typeface"))
    open fun typeface(context: Context): Typeface {
        return typeface
    }

    @Deprecated("20200806 use variable field instead", ReplaceWith("textSize"))
    open fun textSizeSp(context: Context): Float {
        return textSize
    }

    @Deprecated("20200806 use variable field instead", ReplaceWith("textColor"))
    @ColorInt
    open fun color(context: Context): Int {
        return textColor
    }

    @Deprecated("20200806 use variable field instead", ReplaceWith("backgroundColor"))
    @ColorInt
    open fun backgroundColor(context: Context): Int? {
        return backgroundColor
    }

    @Deprecated("20200806 use variable field instead", ReplaceWith("false"))
    open fun isBold(context: Context) = false

    fun textSizePx(context: Context): Float {
        return when (textSizeUnit) {
            TypedValue.COMPLEX_UNIT_SP -> context.sp(textSize)
            TypedValue.COMPLEX_UNIT_DIP -> context.dp(textSize)
            else -> textSize
        }
    }

    abstract fun getText(context: Context): CharSequence?

    override fun measure(context: Context, rowShareElements: RowShareElements) {
        super.measure(context, rowShareElements)
        val paint = rowShareElements.getPaint(textSizePx(context), textColor, typeface)

        val text = getText(context)
        // if nothing changed, ignore measure process
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

        val widthPx = context.dp(width).toInt()
        val minWidthPx = context.dp(minWidth).toInt()
        val isWrapWidth = width == ViewGroup.LayoutParams.WRAP_CONTENT
        if (isWrapWidth) {
            val minWidth = max(widthPx, minWidthPx)
            val minWidthWithMargins = leftMargin + minWidth + rightMargin
            widthWithMargins =
                leftMargin + paddingLeft + measuredTextWidth + paddingRight + rightMargin
            widthWithMargins = max(minWidthWithMargins, widthWithMargins)
        } else {
            widthWithMargins = leftMargin + widthPx + rightMargin
        }

        val heightPx = context.dp(height).toInt()
        val minHeightPx = context.dp(minHeight).toInt()
        val isWrapHeight = height == ViewGroup.LayoutParams.WRAP_CONTENT
        if (isWrapHeight) {
            val minHeight = max(heightPx, minHeightPx)
            val minHeightWithMargins = topMargin + minHeight + bottomMargin
            heightWithMargins =
                topMargin + paddingTop + measuredTextHeight + paddingBottom + bottomMargin
            heightWithMargins = max(minHeightWithMargins, heightWithMargins)
        } else {
            heightWithMargins = topMargin + heightPx + bottomMargin
        }

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

        val contentLeft = left + leftMargin + paddingLeft
        val contentTop = top + topMargin + paddingTop
        val contentRight = right - rightMargin - paddingRight
        val contentBottom = bottom - bottomMargin - paddingBottom
        val rectLayout = rowShareElements.rect1
        rectLayout.set(contentLeft, contentTop, contentRight, contentBottom)

        val rectDraw = rowShareElements.rect2
        Gravity.apply(gravity, measuredTextWidth, measuredTextHeight, rectLayout, rectDraw)
        drawRegionLeft = rectDraw.left
        drawRegionTop = rectDraw.top
        drawRegionRight = rectDraw.right
        drawRegionBottom = rectDraw.bottom
    }

    override fun prepareToDraw(context: Context, rowShareElements: RowShareElements) {
        super.prepareToDraw(context, rowShareElements)

        val paint = rowShareElements.getPaint(textSizePx(context), textColor, typeface)

        val text = getText(context)
        if (TextUtils.isEmpty(text)) {
            boringLayout = null
            staticLayout = null
            dynamicLayout = null
            return
        }

        if (text is String && !drawSimpleTextDirectly) {
            if (null != boringLayout && text == boringLayout!!.text) return
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
        } else if (text is Spannable) {
            if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
                if (null != staticLayout && text == staticLayout!!.text) return
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
                        1F,
                        0F,
                        true
                    )
                }
            } else {
                if (null != dynamicLayout && text == dynamicLayout!!.text) return
                dynamicLayout = DynamicLayout(
                    text,
                    paint,
                    Int.MAX_VALUE,
                    Layout.Alignment.ALIGN_NORMAL,
                    1F,
                    0F,
                    true
                )
            }
        }
    }

    override fun draw(
        context: Context,
        canvas: Canvas,
        rowShareElements: RowShareElements
    ) {
        super.draw(context, canvas, rowShareElements)
        if (null != backgroundColor) {
            val backgroundPaint = rowShareElements.backgroundPaint
            if (backgroundColor != backgroundPaint.color) backgroundPaint.color = backgroundColor!!
            canvas.drawRect(
                columnLeft.toFloat(),
                columnTop.toFloat(),
                columnRight.toFloat(),
                columnBottom.toFloat(),
                backgroundPaint
            )
        }

        val paint = rowShareElements.getPaint(textSizePx(context), textColor, typeface)
        val text = getText(context) ?: ""

        val drawLeft = drawRegionLeft.toFloat()
        val drawTop = drawRegionTop.toFloat()
        val drawRegionHeight = drawRegionBottom - drawRegionTop
        canvas.save()
        if (text is String && null != boringLayout) {
            canvas.translate(drawLeft, drawTop + boringLayout!!.topPadding)
            boringLayout?.draw(canvas)
        } else if (text is Spannable) {
            if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
                if (null != staticLayout) {
                    canvas.translate(drawLeft, drawTop + staticLayout!!.topPadding)
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
                    canvas.translate(drawLeft, drawTop + dynamicLayout!!.topPadding)
                    dynamicLayout?.draw(canvas)
                } else {
                    canvas.drawText(
                        text,
                        0,
                        text.length,
                        drawLeft,
                        drawTop + drawRegionHeight - paint.descent(),
                        paint
                    )
                }
            }
        } else {
            canvas.drawText(
                text.toString(),
                drawLeft,
                drawTop + drawRegionHeight - paint.descent(),
                paint
            )
        }
        canvas.restore()
    }

    override fun shouldIgnoreDraw(container: View): Boolean {
        val left = container.scrollX
        val right = left + container.width
        val top = container.scrollY
        val bottom = top + container.height
        return drawRegionRight < left || drawRegionLeft > right || drawRegionBottom < top || drawRegionTop > bottom
    }

}