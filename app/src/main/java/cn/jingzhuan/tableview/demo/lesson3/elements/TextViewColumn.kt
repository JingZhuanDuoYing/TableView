package cn.jingzhuan.tableview.demo.lesson3.elements

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import cn.jingzhuan.tableview.demo.R
import cn.jingzhuan.tableview.element.ViewColumn

class TextViewColumn : ViewColumn() {

    override fun createView(context: Context): View {
        return LayoutInflater.from(context).inflate(R.layout.text_view, null)
    }

    override fun bindView(view: View) {
        if(view !is TextView) return
        view.setTextColor(Color.RED)
        view.text = "I'm red"
    }

}