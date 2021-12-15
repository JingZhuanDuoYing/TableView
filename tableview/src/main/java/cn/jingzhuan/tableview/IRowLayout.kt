package cn.jingzhuan.tableview

import android.view.View
import android.view.ViewParent
import cn.jingzhuan.tableview.element.Row
import cn.jingzhuan.tableview.layoutmanager.ColumnsLayoutManager

interface IRowLayout {

    fun isIndependentScrollRange(): Boolean

    fun bindRow(row: Row<*>, layoutManager: ColumnsLayoutManager)

    fun updateScrollX(scrollX: Int)

    fun onScrollTo(x: Int, y: Int)

    fun onScrollBy(x: Int)

    fun onGetScrollX(): Int

    fun realChildCount(): Int

    fun doLayout()

    fun onGetParentView(): ViewParent?

    fun onGetChildAt(index: Int): View?

    fun onGetRow(): Row<*>?

}