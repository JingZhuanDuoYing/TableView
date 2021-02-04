package cn.jingzhuan.tableview.demo.lesson3.elements

import android.graphics.Color
import cn.jingzhuan.tableview.demo.R
import cn.jingzhuan.tableview.demo.databinding.TextViewBinding
import cn.jingzhuan.tableview.element.DataBindingViewColumn
import cn.jingzhuan.tableview.element.Row

class DataBindingTextViewColumn: DataBindingViewColumn<TextViewBinding>() {

    override fun layoutId() = R.layout.text_view

    override fun onBind(binding: TextViewBinding, row: Row<*>) {
        binding.textView.setTextColor(Color.BLUE)
        binding.textView.text = "I'm databinding blue"
    }

}