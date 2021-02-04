package cn.jingzhuan.tableview.demo.lesson3.elements

import cn.jingzhuan.tableview.demo.R
import cn.jingzhuan.tableview.demo.databinding.ImageViewBinding
import cn.jingzhuan.tableview.element.DataBindingViewColumn
import cn.jingzhuan.tableview.element.Row

class ImageViewColumn : DataBindingViewColumn<ImageViewBinding>() {

    override fun layoutId() = R.layout.image_view

    override fun onBind(binding: ImageViewBinding, row: Row<*>) {
        binding.imageView.setImageResource(R.drawable.lion)
    }

}