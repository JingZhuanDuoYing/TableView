package cn.jingzhuan.tableview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import android.os.Looper
import android.util.TypedValue
import android.view.View
import android.view.WindowManager

internal fun <T> lazyNone(initializer: () -> T) = lazy(LazyThreadSafetyMode.NONE, initializer)

internal fun Context.dp(value: Float): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics)
}

internal fun Context.dp(value: Int): Float {
    return dp(value.toFloat())
}

internal fun Context.sp(value: Float): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, resources.displayMetrics)
}

internal fun Context.screenWidth(): Int {
    val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val point = Point()
    wm.defaultDisplay.getSize(point)
    return point.x
}

internal fun Canvas.drawRect(
    left: Int,
    top: Int,
    right: Int,
    bottom: Int,
    paint: Paint
) =
    drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)

internal fun Canvas.drawLine(
    left: Int,
    top: Int,
    right: Int,
    bottom: Int,
    paint: Paint
) = drawLine(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)

internal fun View.runOnMainThread(action: () -> Unit) {
    if(Looper.getMainLooper() == Looper.myLooper()) {
        action()
    } else {
        post(action)
    }
}
