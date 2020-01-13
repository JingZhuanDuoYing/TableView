package cn.jingzhuan.tableview.demo.elements

import android.content.Context
import android.view.Gravity
import cn.jingzhuan.tableview.demo.dp
import cn.jingzhuan.tableview.element.TextColumn

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