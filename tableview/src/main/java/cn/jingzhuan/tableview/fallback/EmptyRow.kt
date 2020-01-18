package cn.jingzhuan.tableview.fallback

import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import cn.jingzhuan.tableview.adapter.IRowListAdapterDelegate
import cn.jingzhuan.tableview.element.Row

internal class EmptyRow : Row<EmptyColumn>(listOf()) {

    override fun createView(context: Context): ViewGroup {
        val view = FrameLayout(context)
        view.tag = EmptyRow::class.java.name
        return view
    }

    override fun type(): Int {
        return IRowListAdapterDelegate.INVALID_VIEW_TYPE
    }

}