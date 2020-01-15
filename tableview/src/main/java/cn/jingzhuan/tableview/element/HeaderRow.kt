package cn.jingzhuan.tableview.element

import android.content.Context
import cn.jingzhuan.tableview.adapter.IRowListAdapterDelegate
import cn.jingzhuan.tableview.layoutmanager.ColumnsLayoutManager

open class HeaderRow<COLUMN : Column>(columns: List<COLUMN>) : Row<COLUMN>(columns) {

    val stickyRows = mutableListOf<Row<*>>()
    val rows = mutableListOf<Row<*>>()
    internal var layoutManager: ColumnsLayoutManager? = null

    /**
     * 子类除特殊情况请不要重写此方法
     */
    override fun type(): Int {
        return IRowListAdapterDelegate.HEADER_VIEW_TYPE
    }

    override fun visible(): Boolean {
        return true
    }

    fun preMeasureAllRows(context: Context) {
        val layoutManager = layoutManager ?: return
        measure(context, layoutManager.specs)
        stickyRows.forEach { it.measure(context, layoutManager.specs) }
        rows.forEach { it.measure(context, layoutManager.specs) }
    }

}