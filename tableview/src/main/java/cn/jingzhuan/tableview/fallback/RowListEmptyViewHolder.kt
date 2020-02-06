package cn.jingzhuan.tableview.fallback

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.widget.FrameLayout

internal class RowListEmptyViewHolder(container: ViewGroup) :
    RecyclerView.ViewHolder(FrameLayout(container.context).apply { tag = "Test" })