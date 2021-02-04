package cn.jingzhuan.tableview.element

import android.content.Context
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View

abstract class DataBindingViewColumn<BINDING : ViewDataBinding> : ViewColumn {

    private var binding: BINDING? = null

    constructor() : super()
    constructor(
        minWidth: Int? = null,
        minHeight: Int? = null,
        width: Int? = null,
        height: Int? = null,
        leftMargin: Int? = null,
        topMargin: Int? = null,
        rightMargin: Int? = null,
        bottomMargin: Int? = null,
        paddingLeft: Int? = null,
        paddingTop: Int? = null,
        paddingRight: Int? = null,
        paddingBottom: Int? = null,
        gravity: Int? = null,
        visible: Boolean? = null
    ) : super(
        minWidth,
        minHeight,
        width,
        height,
        leftMargin,
        topMargin,
        rightMargin,
        bottomMargin,
        paddingLeft,
        paddingTop,
        paddingRight,
        paddingBottom,
        gravity,
        visible
    )

    @LayoutRes
    abstract fun layoutId(): Int

    @Deprecated("replace with onBind(binding: BINDING, row: Row<*>)")
    open fun onBind(binding: BINDING) {}

    abstract fun onBind(binding: BINDING, row: Row<*>)

    override fun createView(context: Context): View {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), layoutId(), null, false)
        return binding!!.root
    }

    override fun bindView(view: View, row: Row<*>) {
        val binding = binding ?: return
        if (view != binding.root) return
        onBind(binding, row)
    }

    override fun bindView(view: View) {
        val binding = binding ?: return
        if (view != binding.root) return
        onBind(binding)
    }

}