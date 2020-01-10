package com.nagihong.tableview.fallback

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.widget.FrameLayout

internal class RowListEmptyViewHolder(container: ViewGroup) :
    RecyclerView.ViewHolder(FrameLayout(container.context))