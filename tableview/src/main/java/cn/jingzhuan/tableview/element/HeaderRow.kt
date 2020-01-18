package cn.jingzhuan.tableview.element

import android.content.Context
import cn.jingzhuan.tableview.adapter.IRowListAdapterDelegate
import cn.jingzhuan.tableview.layoutmanager.ColumnsLayoutManager
import cn.jingzhuan.tableview.layoutmanager.TableSpecs

open class HeaderRow<COLUMN : Column>(columns: List<COLUMN>) : Row<COLUMN>(columns) {

    @Transient
    var stickyRows = mutableListOf<Row<*>>()
        private set
    @Transient
    var rows = mutableListOf<Row<*>>()
        private set
    @Transient
    internal var layoutManager: ColumnsLayoutManager? = null

    init {
        stickyRows = mutableListOf()
        rows = mutableListOf()
    }

    /**
     * 子类除特殊情况请不要重写此方法
     */
    override fun type(): Int {
        return IRowListAdapterDelegate.HEADER_VIEW_TYPE
    }

    fun setColumnVisibility(index: Int, visible: Boolean) {
        columns[index].visible = visible
    }

    fun setColumnVisibility(column: Column, visible: Boolean) {
        column.visible = visible
    }

    fun preMeasureAllRows(context: Context) {
        val layoutManager = layoutManager ?: return
        measure(context, layoutManager.specs)
        stickyRows.forEach { it.measure(context, layoutManager.specs) }
        rows.forEach { it.measure(context, layoutManager.specs) }
    }

    fun getLayoutManager(): ColumnsLayoutManager? {
        return layoutManager
    }

    fun getTableSpecs(): TableSpecs? {
        return layoutManager?.specs
    }

}