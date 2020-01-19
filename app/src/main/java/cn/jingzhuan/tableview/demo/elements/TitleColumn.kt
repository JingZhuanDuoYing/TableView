package cn.jingzhuan.tableview.demo.elements

import android.content.Context
import cn.jingzhuan.tableview.element.TextColumn

class TitleColumn(private val index: Int) : TextColumn() {

    override fun getText(context: Context): CharSequence? {
        return "$index"
    }
}