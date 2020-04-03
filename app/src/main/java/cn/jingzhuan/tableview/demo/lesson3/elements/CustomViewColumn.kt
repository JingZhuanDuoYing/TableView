package cn.jingzhuan.tableview.demo.lesson3.elements

import android.content.Context
import android.view.View
import cn.jingzhuan.tableview.element.ViewColumn

class CustomViewColumn: ViewColumn() {

    override fun createView(context: Context): View {
        return CustomView(context)
    }

    override fun bindView(view: View) {
        
    }

}