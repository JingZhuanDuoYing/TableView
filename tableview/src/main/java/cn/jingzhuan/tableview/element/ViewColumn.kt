package cn.jingzhuan.tableview.element

import android.content.Context
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup.MarginLayoutParams

abstract class ViewColumn : Column() {

    internal var forceLayout = true

    internal var left = 0
    internal var top = 0
    internal var right = 0
    internal var bottom = 0

    abstract fun createView(context: Context): View

    abstract fun bindView(view: View)

    override fun layout(
        context: Context,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        rowShareElements: RowShareElements
    ) {
        super.layout(context, left, top, right, bottom, rowShareElements)
        this.left = left
        this.top = top
        this.right = right
        this.bottom = bottom
    }

    open fun measureView(view: View) {
        val lp = view.layoutParams
        val widthSpec =
            if (lp.width > 0) MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY)
            else MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        val heightSpec =
            if (lp.height > 0) MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY)
            else MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        view.measure(widthSpec, heightSpec)

        val mlp = lp as? MarginLayoutParams
        val leftMargin = mlp?.leftMargin ?: 0
        val topMargin = mlp?.topMargin ?: 0
        val rightMargin = mlp?.rightMargin ?: 0
        val bottomMargin = mlp?.bottomMargin ?: 0
        widthWithMargins = view.measuredWidth + leftMargin + rightMargin
        heightWithMargins = view.measuredHeight + topMargin + bottomMargin
    }

    open fun layoutView(view: View, forceLayout: Boolean = false) {
        val mlp = view.layoutParams as? MarginLayoutParams
        val leftMargin = mlp?.leftMargin ?: 0
        val topMargin = mlp?.topMargin ?: 0
        val rightMargin = mlp?.rightMargin ?: 0
        val bottomMargin = mlp?.bottomMargin ?: 0

        val viewLeft = left + leftMargin
        val viewTop = top + topMargin
        val viewRight = right - rightMargin
        val viewBottom = bottom - bottomMargin

        if (forceLayout || view.left != viewLeft || view.top != viewTop || view.right != viewRight || view.bottom != viewBottom) {
            view.layout(viewLeft, viewTop, viewRight, viewBottom)
        }
    }

    /**
     * @return true to ignore next layout
     */
    open fun checkLayout(view: View): Boolean {
        val mlp = view.layoutParams as? MarginLayoutParams
        val leftMargin = mlp?.leftMargin ?: 0
        val topMargin = mlp?.topMargin ?: 0
        val rightMargin = mlp?.rightMargin ?: 0
        val bottomMargin = mlp?.bottomMargin ?: 0

        if (view.width == 0 || view.height == 0) return false
        if (leftMargin + view.measuredWidth + rightMargin != widthWithMargins) return false
        if (topMargin + view.measuredHeight + bottomMargin != heightWithMargins) return false

        val viewLeft = left + leftMargin
        val viewTop = top + topMargin
        val viewRight = right - rightMargin
        val viewBottom = bottom - bottomMargin

        if (view.left != viewLeft) return false
        if (view.top != viewTop) return false
        if (view.right != viewRight) return false
        if (view.bottom != viewBottom) return false
        return true
    }

    fun forceLayout() {
        forceLayout = true
    }

}