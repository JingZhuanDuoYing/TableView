package cn.jingzhuan.tableview.element

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

abstract class DataBindingViewColumn<BINDING : ViewDataBinding> : ViewColumn() {

    private var binding: BINDING? = null

    @LayoutRes
    abstract fun layoutId(): Int

    abstract fun onBind(binding: BINDING)

    override fun createView(context: Context): View {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), layoutId(), null, false)
        return binding!!.root
    }

    override fun bindView(view: View) {
        val binding = binding ?: return
        if (view != binding.root) return
        onBind(binding)
    }

}