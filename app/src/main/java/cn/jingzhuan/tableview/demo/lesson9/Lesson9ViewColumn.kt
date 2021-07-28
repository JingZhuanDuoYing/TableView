package cn.jingzhuan.tableview.demo.lesson9

import cn.jingzhuan.tableview.demo.R
import cn.jingzhuan.tableview.demo.databinding.LayoutLesson9ViewColumnBinding
import cn.jingzhuan.tableview.element.DataBindingViewColumn
import cn.jingzhuan.tableview.element.Row

class Lesson9ViewColumn: DataBindingViewColumn<LayoutLesson9ViewColumnBinding>() {

    override fun layoutId() = R.layout.layout_lesson_9_view_column

    override fun onBind(binding: LayoutLesson9ViewColumnBinding, row: Row<*>) {

    }
}