package cn.jingzhuan.tableview.element

import android.content.Context
import cn.jingzhuan.tableview.adapter.IRowListAdapterDelegate
import cn.jingzhuan.tableview.layoutmanager.ColumnsLayoutManager
import cn.jingzhuan.tableview.layoutmanager.TableSpecs
import java.io.ObjectInputStream
import java.util.*

open class HeaderRow<COLUMN : Column>(columns: List<COLUMN>) : Row<COLUMN>(columns) {

    @Transient
    var stickyRows = mutableListOf<Row<*>>()
        private set
    @Transient
    var rows = mutableListOf<Row<*>>()
        private set
    @Transient
    var layoutManager: ColumnsLayoutManager? = null

    init {
        stickyRows = Collections.synchronizedList(mutableListOf())
        rows = Collections.synchronizedList(mutableListOf())
    }

    private fun readObject(inputStream: ObjectInputStream) {
        inputStream.defaultReadObject()
        stickyRows = Collections.synchronizedList(mutableListOf())
        rows = Collections.synchronizedList(mutableListOf())
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
        var columnsWidthChanged = false
        columnsWidthChanged = columnsWidthChanged or measure(context, layoutManager.specs)
        stickyRows.forEach { columnsWidthChanged = columnsWidthChanged or it.measure(context, layoutManager.specs) }
        rows.forEach { columnsWidthChanged = columnsWidthChanged or it.measure(context, layoutManager.specs) }
        if(columnsWidthChanged) getTableSpecs()?.onColumnsWidthChanged()
    }

    fun preMeasureRow(context: Context, row: Row<*>) {
        val layoutManager = layoutManager ?: return
        if (row.measure(context, layoutManager.specs)) {
            getTableSpecs()?.onColumnsWidthChanged()
            row.forceLayout = true
        }
    }

    fun preLayoutRowIfNecessary(context: Context, row: Row<*>) {
        val layoutManager = layoutManager ?: return
        if (row.forceLayout) row.layout(context, layoutManager.specs)
    }

    fun getTableSpecs(): TableSpecs? {
        return layoutManager?.specs
    }

}