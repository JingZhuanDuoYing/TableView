package cn.jingzhuan.tableview.demo.elements

import android.content.Context
import android.view.Gravity
import cn.jingzhuan.tableview.element.TextColumn

class TitleHeaderColumn : TextColumn() {

    init {
        gravity = Gravity.CENTER
        paddingRight = 20
    }

    override fun getText(context: Context): CharSequence? {
        return "Title"
    }

    override fun visible(): Boolean {
        return true
    }

}