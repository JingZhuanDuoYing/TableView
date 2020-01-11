package com.nagihong.tableview.demo.lesson1.elements

import android.content.Context
import android.view.Gravity
import com.nagihong.tableview.demo.dp
import com.nagihong.tableview.element.TextColumn

class TitleHeaderColumn : TextColumn() {

    override fun getText(context: Context): CharSequence? {
        return "Title"
    }

    override fun padding(context: Context): Array<Int> {
        return arrayOf(0, 0, context.dp(12F).toInt(), 0)
    }

    override fun gravity(): Int {
        return Gravity.CENTER
    }

    override fun visible(): Boolean {
        return true
    }

}