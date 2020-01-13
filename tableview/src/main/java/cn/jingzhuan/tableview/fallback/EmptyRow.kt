package cn.jingzhuan.tableview.fallback

import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import cn.jingzhuan.tableview.TableViewLog
import cn.jingzhuan.tableview.adapter.IRowListAdapterDelegate
import cn.jingzhuan.tableview.element.Row
import cn.jingzhuan.tableview.layoutmanager.ColumnsLayoutManager

internal class EmptyRow : Row<EmptyColumn>(listOf()) {

    override fun createView(context: Context): ViewGroup {
        return FrameLayout(context)
    }

    override fun bindView(view: ViewGroup, layoutManager: ColumnsLayoutManager) {
        TableViewLog.w(this::class.java.name, "found an EmptyRow.")
    }

    override fun type(): Int {
        return IRowListAdapterDelegate.INVALID_VIEW_TYPE
    }

    override fun visible(): Boolean {
        return true
    }

}