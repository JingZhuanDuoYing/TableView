package cn.jingzhuan.tableview.adapter

import android.util.SparseArray
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.jingzhuan.tableview.RowListViewHolder
import cn.jingzhuan.tableview.TableViewLog
import cn.jingzhuan.tableview.element.HeaderRow
import cn.jingzhuan.tableview.element.Row
import cn.jingzhuan.tableview.fallback.EmptyRow
import cn.jingzhuan.tableview.fallback.RowListEmptyViewHolder
import java.util.*

open class RowListAdapterDelegate : IRowListAdapterDelegate {

    override var headerRow: HeaderRow<*>? = null

    private val adapterDataObserver = AdapterDataObserver { onAdapterDataChanged() }
    private val adapters = mutableListOf<RowListAdapter>()

    private val expandedRowsSnapshot = Collections.synchronizedList(mutableListOf<Row<*>>())

    private val headerTypeRowMap = SparseArray<Row<*>>()
    private val typeRowMap = SparseArray<Row<*>>()
    private val childrenTypeRowMap = SparseArray<Row<*>>()

    override fun createViewHolder(
        parent: ViewGroup,
        viewType: Int,
        fromHeader: Boolean
    ): RecyclerView.ViewHolder {
        val row = (if (fromHeader) {
            getHeaderRowForType(viewType)
        } else {
            getRowForType(viewType)
        }) ?: return RowListEmptyViewHolder(
            parent
        )

        val rowView = row.createView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        return RowListViewHolder(rowView)
    }

    override fun bindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        fromHeader: Boolean
    ) {
        if (holder is RowListEmptyViewHolder) {
            TableViewLog.e(
                this::class.java.name,
                "found an RowListEmptyViewHolder at $position${if (fromHeader) "(header)" else ""}"
            )
            return
        }

        val row = getRow(position, fromHeader)
        if (null == row) {
            TableViewLog.e(this::class.java.simpleName, "row should not be null")
            return
        }

        val headerRow = headerRow
        if (null == headerRow) {
            TableViewLog.e(this::class.java.simpleName, "HeaderRow should not be null")
            return
        }

        val layoutManager = headerRow.layoutManager
        if (null == layoutManager) {
            TableViewLog.e(this::class.java.simpleName, "LayoutManager should not be null")
            return
        }

        (holder as? RowListViewHolder)?.bindData(row, layoutManager)
    }

    override fun getItemCount(fromHeader: Boolean): Int {
        val headerRow = headerRow ?: return 0
        return if (fromHeader) {
            headerRow.stickyRows.size + 1
        } else {
            headerRow.rows.size
        }
    }

    override fun getItemViewType(
        position: Int,
        fromHeader: Boolean
    ): Int {
        val headerRow = headerRow ?: return IRowListAdapterDelegate.INVALID_VIEW_TYPE
        return if (fromHeader) {
            if (position == 0) headerRow.type()
            else headerRow.stickyRows.getOrNull(position - 1)?.type()
                ?: IRowListAdapterDelegate.INVALID_VIEW_TYPE
        } else {
            headerRow.rows.getOrNull(position)?.type() ?: IRowListAdapterDelegate.INVALID_VIEW_TYPE
        }
    }

    override fun getItemId(
        position: Int,
        fromHeader: Boolean
    ): Long {
        return position.toLong()
    }

    override fun notifyDataSetChanged() {
        adapters.forEach { it.notifyDataSetChanged() }
    }

    override fun connect(adapter: RowListAdapter) {
        if (!adapters.contains(adapter)) {
            adapters.add(adapter)
//            adapter.registerAdapterDataObserver(adapterDataObserver)
        }
        adapter.delegate = this
    }

    private fun onAdapterDataChanged() {
//        headerRow?.rows?.filter {  }
    }

    private fun getRow(
        position: Int,
        fromHeader: Boolean
    ): Row<*>? {
        val headerRow = headerRow ?: return EmptyRow()
        if (fromHeader) {
            if (position == 0) return headerRow
            return headerRow.stickyRows.getOrNull(position - 1) ?: EmptyRow()
        } else {
            return headerRow.rows.getOrNull(position) ?: EmptyRow()
        }
    }

    private fun getRowForType(type: Int): Row<*>? {
        if (type == IRowListAdapterDelegate.INVALID_VIEW_TYPE) return EmptyRow()
        val headerRow = headerRow ?: return EmptyRow()
        if(null != typeRowMap[type]) return typeRowMap[type]

        headerRow.rows.distinctBy { it.type() }.forEach {
            typeRowMap.put(it.type(), it)
        }
        return typeRowMap[type] ?: EmptyRow()
    }

    private fun getHeaderRowForType(type: Int): Row<*>? {
        if (type == IRowListAdapterDelegate.INVALID_VIEW_TYPE) return EmptyRow()
        val headerRow = headerRow ?: return EmptyRow()
        if (type == IRowListAdapterDelegate.HEADER_VIEW_TYPE) return headerRow
        if(null != headerTypeRowMap[type]) return headerTypeRowMap[type]

        headerRow.stickyRows.distinctBy { it.type() }.forEach {
            headerTypeRowMap.put(it.type(), it)
        }
        return headerTypeRowMap[type] ?: EmptyRow()
    }

}