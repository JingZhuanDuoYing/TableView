package cn.jingzhuan.tableview

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.jingzhuan.tableview.element.Row
import cn.jingzhuan.tableview.layoutmanager.ColumnsLayoutManager

/**
 * Chenyikang
 * 2018 December 22
 */
internal class RowListViewHolder(
    container: ViewGroup,
    rowElement: Row<*>
) : RecyclerView.ViewHolder(
    rowElement.createView(container.context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
) {

    private lateinit var data: Row<*>

    fun bindData(
        data: Row<*>,
        layoutManager: ColumnsLayoutManager
    ) {
        this.data = data
        (itemView as? RowLayout)?.bindRow(data, layoutManager)
        data.onBindView(itemView, layoutManager)
    }

}