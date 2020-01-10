package com.nagihong.tableview.fallback

import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import com.nagihong.tableview.adapter.IRowListDelegate
import com.nagihong.tableview.element.Row

internal class EmptyRow : Row<EmptyColumn>(listOf()) {

    override fun createView(context: Context): ViewGroup {
        return FrameLayout(context)
    }

    override fun bindView(view: ViewGroup) {

    }

    override fun type(): Int {
        return IRowListDelegate.INVALID_VIEW_TYPE
    }

    override fun id(): Long {
        return IRowListDelegate.INVALID_ITEM_ID
    }

    override fun visible(): Boolean {
        return true
    }

}