package cn.jingzhuan.tableview

import android.support.v7.widget.RecyclerView
import android.view.View
import cn.jingzhuan.tableview.element.Row
import cn.jingzhuan.tableview.layoutmanager.ColumnsLayoutManager

/**
 * Chenyikang
 * 2018 December 22
 */
internal class RowListViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private lateinit var data: Row<*>

    fun bindData(
        data: Row<*>,
        layoutManager: ColumnsLayoutManager
    ) {
        this.data = data
        if (itemView is RowLayout) {
            itemView.bindRow(data, layoutManager)
        } else {
            data.onBindView(itemView, layoutManager)
        }
    }

}