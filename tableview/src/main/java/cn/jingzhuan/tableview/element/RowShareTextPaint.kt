package cn.jingzhuan.tableview.element

import android.graphics.Paint
import android.text.TextPaint

class RowShareTextPaint : TextPaint {

    constructor() : super()
    constructor(flags: Int) : super(flags)
    constructor(p: Paint?) : super(p)
    internal var doOnRelease: ((RowShareTextPaint) -> Unit)? = null

    @Transient
    var acquired: Boolean = false

    fun acquire(): RowShareTextPaint {
        acquired = true
        return this
    }

    fun release() {
        acquired = false
        doOnRelease?.invoke(this)
    }

}