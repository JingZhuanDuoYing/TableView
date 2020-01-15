package cn.jingzhuan.tableview.demo.lesson3.elements

import cn.jingzhuan.tableview.demo.R
import cn.jingzhuan.tableview.demo.databinding.ImageViewBinding
import cn.jingzhuan.tableview.element.DataBindingViewColumn

class ImageViewColumn : DataBindingViewColumn<ImageViewBinding>() {

    override fun layoutId() = R.layout.image_view

    override fun onBind(binding: ImageViewBinding) {
        binding.imageView.setImageResource(R.drawable.lion)
    }

}