package cn.jingzhuan.tableview.demo.elements

import android.content.Context
import cn.jingzhuan.tableview.element.TextColumn

class SimpleColumn(var value: String) : TextColumn() {

    override fun getText(context: Context): CharSequence? {
        return value
    }

}