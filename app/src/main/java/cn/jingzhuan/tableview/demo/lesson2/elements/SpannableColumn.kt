package cn.jingzhuan.tableview.demo.lesson2.elements

import android.content.Context
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import cn.jingzhuan.tableview.element.RowShareElements
import cn.jingzhuan.tableview.element.TextColumn

class SpannableColumn(
    private val redText: String,
    private val greenText: String,
    private val blueText: String
) : TextColumn() {

    private var spannable: SpannableString? = null

    override fun prepareToMeasure(context: Context, rowShareElements: RowShareElements) {
        super.prepareToMeasure(context, rowShareElements)
        if (null != spannable) return
        val sb = SpannableString("$redText$greenText$blueText")
        sb.setSpan(
            ForegroundColorSpan(Color.RED),
            0,
            redText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        sb.setSpan(
            ForegroundColorSpan(Color.GREEN),
            redText.length,
            redText.length + greenText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        sb.setSpan(
            ForegroundColorSpan(Color.BLUE),
            redText.length + greenText.length,
            sb.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable = sb
    }

    override fun getText(context: Context): CharSequence? {
        return spannable
    }

}