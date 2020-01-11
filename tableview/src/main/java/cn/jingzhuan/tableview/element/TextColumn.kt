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
import kotlin.math.max

abstract class TextColumn : DrawableColumn() {

    @Transient
    private var rectLayout: Rect? = null
    @Transient
    private var rectDraw: Rect? = null

    @Transient
    private var paint: TextPaint? = null
    @Transient
    private var paintInBackgroundThread: TextPaint? = null

    @Transient
    private var path: Path? = null
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

    override fun measure(context: Context) {
        super.measure(context)
        val paint = getPaint(context)
        val paintInBackgroundThread = getPaintInBackgroundThread(context)

        val color = color(context)
        if (paint.color != color) paint.color = color
        if (paintInBackgroundThread.color != color) paintInBackgroundThread.color = color

        val text = getText(context)
        if (TextUtils.equals(
                text, lastMeasuredValue
            ) && widthWithMargins > 0 && heightWithMargins > 0
        ) return

        if (TextUtils.isEmpty(text)) {
            measuredTextWidth = 0
            measuredTextHeight = 0
        } else {
            val rectLayout = getRectLayout()
            paintInBackgroundThread.getTextBounds(text.toString(), 0, text!!.length, rectLayout)
            measuredTextWidth = max(rectLayout.width(), 0)
            measuredTextHeight = rectLayout.height()
        }

        val margins = margins(context)
        val padding = padding(context)

        if (!TextUtils.isEmpty(text) && text is String) {
            // before lollipop, BoringLayout.isBoring may return null
            val params =
                BoringLayout.isBoring(text, paintInBackgroundThread)
                    ?: BoringLayout.Metrics().apply {
                        top = paint.fontMetricsInt.top
                        ascent = paint.fontMetricsInt.ascent
                        descent = paint.fontMetricsInt.descent
                        bottom = paint.fontMetricsInt.bottom
                        leading = paint.fontMetricsInt.leading
                        this.width = measuredTextWidth
                    }
            // when meet chinese words, BoringLayout on some devices may not working
            if (null == boringLayout) {
                boringLayout = BoringLayout.make(
                    text,
                    paintInBackgroundThread,
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
                    paintInBackgroundThread,
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
            staticLayout = if (VERSION.SDK_INT >= VERSION_CODES.M) {
                StaticLayout.Builder.obtain(text, 0, text.length, paint, Int.MAX_VALUE)
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
            measuredTextWidth = max(staticLayout!!.getLineWidth(0).toInt(), 0)
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
        bottom: Int
    ) {
        super.layout(context, left, top, right, bottom)
        val margins = margins(context)
        val padding = padding(context)

        val contentLeft = left + margins[0] + padding[0]
        val contentTop = top + margins[1] + padding[1]
        val contentRight = right - margins[2] - padding[2]
        val contentBottom = bottom - margins[3] - padding[3]
        val rectLayout = getRectLayout()
        rectLayout.set(contentLeft, contentTop, contentRight, contentBottom)

        val gravity = gravity()
        val rectDraw = getRectDraw()
        Gravity.apply(gravity, measuredTextWidth, measuredTextHeight, rectLayout, rectDraw)
    }

    override fun shouldIgnoreDraw(container: View): Boolean {
        val left = container.scrollX
        val right = left + container.width
        val top = container.scrollY
        val bottom = top + container.height
        val rectDraw = getRectDraw()
        return rectDraw.right < left || rectDraw.left > right || rectDraw.bottom < top || rectDraw.top > bottom
    }

    override fun draw(
        context: Context,
        canvas: Canvas
    ) {
        super.draw(context, canvas)

        val paint = getPaint(context)
        val rectDraw = getRectDraw()
        val text = getText(context) ?: ""

        val drawLeft = rectDraw.left.toFloat()
        val drawTop = rectDraw.top.toFloat()
        canvas.save()
        if (text is String && null != boringLayout) {
            if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
                canvas.translate(drawLeft, drawTop)
            } else {
                canvas.translate(drawLeft, drawTop - paint.descent())
            }
            boringLayout?.draw(canvas)
        } else if (text is Spannable && null != staticLayout) {
            canvas.translate(drawLeft, drawTop - paint.descent())
            staticLayout?.draw(canvas)
        } else {
            canvas.drawText(text.toString(), drawLeft, drawTop + rectDraw.height(), paint)
        }
        canvas.restore()
    }

    private fun getRectLayout(): Rect {
        if (null == rectLayout) rectLayout = Rect()
        return rectLayout!!
    }

    private fun getRectDraw(): Rect {
        if (null == rectDraw) rectDraw = Rect()
        return rectDraw!!
    }

    private fun getPath(): Path {
        if (null == path) path = Path()
        return path!!
    }

    private fun getPaint(context: Context): TextPaint {
        if (null == paint) paint = initPaint(context)
        return paint!!
    }

    private fun initPaint(context: Context): TextPaint {
        val paint = TextPaint()
        paint.isDither = true
        paint.isAntiAlias = true
        paint.textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, textSizeSp(context), context.resources.displayMetrics
        )
        paint.typeface = Typeface.create(
            typeface(context),
            if (isBold(context)) Typeface.BOLD else Typeface.NORMAL
        )
        return paint
    }

    private fun getPaintInBackgroundThread(context: Context): TextPaint {
        if (null == paintInBackgroundThread) paintInBackgroundThread =
            initPaintInBackgroundThread(context)
        return paintInBackgroundThread!!
    }

    private fun initPaintInBackgroundThread(context: Context): TextPaint {
        val paint = TextPaint()
        paint.isDither = true
        paint.isAntiAlias = true
        paint.textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, textSizeSp(context), context.resources.displayMetrics
        )
        paint.typeface = Typeface.create(
            typeface(context),
            if (isBold(context)) Typeface.BOLD else Typeface.NORMAL
        )
        return paint
    }

}