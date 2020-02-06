package cn.jingzhuan.tableview.layoutmanager

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

/**
 * Chenyikang
 * 7/24/18
 */
internal class RowListLayoutManager(
    context: Context,
    private val scrollHorizontallyBy: (dx: Int, remainingScrollHorizontal: Int) -> Int
) : LinearLayoutManager(context) {

    override fun canScrollVertically() = true

    override fun canScrollHorizontally() = true

    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        return scrollHorizontallyBy.invoke(dx, state.remainingScrollHorizontal)
    }
}