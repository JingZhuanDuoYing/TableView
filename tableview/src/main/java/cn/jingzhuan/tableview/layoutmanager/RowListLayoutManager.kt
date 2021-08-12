package cn.jingzhuan.tableview.layoutmanager

import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Chenyikang
 * 7/24/18
 */
internal class RowListLayoutManager(
    context: Context,
    // @return consumed dx
    private val onScrollHorizontallyBy: (dx: Int, remainingScrollHorizontal: Int) -> Int,
    // @return whether to stop scroller
    private val onHorizontalScrollStateChanged: (state: Int, dx: Int) -> Boolean
) : LinearLayoutManager(context) {

    private var lastScrollState = RecyclerView.SCROLL_STATE_IDLE
    private var lastHorizontalScrollState = RecyclerView.SCROLL_STATE_IDLE

    override fun canScrollVertically() = true

    override fun canScrollHorizontally() = true

    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        if (lastHorizontalScrollState != lastScrollState) {
            lastHorizontalScrollState = lastScrollState
            if(onHorizontalScrollStateChanged(lastHorizontalScrollState, dx)) return 0
        }
        return onScrollHorizontallyBy.invoke(dx, state.remainingScrollHorizontal)
    }

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)
        lastScrollState = state
        if (state == RecyclerView.SCROLL_STATE_IDLE && lastHorizontalScrollState != RecyclerView.SCROLL_STATE_IDLE) {
            lastHorizontalScrollState = state
            onHorizontalScrollStateChanged(lastHorizontalScrollState, 0)
        }
    }
}