package cn.jingzhuan.tableview.directionlock

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import kotlin.LazyThreadSafetyMode.NONE

/**
 * Chenyikang
 * 2019 January 11
 */
class DirectionLockRecyclerView : RecyclerView {

  private val lockHelper by lazy(NONE) { MotionDirectionLockHelper() }
  var directionLockEnabled = false
  var enableHorizontalGlow = true
  var enableVerticalGlow = true

  constructor(context: Context) : super(context)
  constructor(
    context: Context,
    attrs: AttributeSet?
  ) : super(context, attrs)

  constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyle: Int
  ) : super(context, attrs, defStyle)

  init {
    edgeEffectFactory =
      DirectionLockRecyclerViewEdgeEffectFactory()
  }

  override fun onInterceptTouchEvent(e: MotionEvent?): Boolean {
    e ?: return super.onInterceptTouchEvent(e)
    if (directionLockEnabled) lockHelper.lock(e)
    return super.onInterceptTouchEvent(e)
  }

  @SuppressLint("ClickableViewAccessibility")
  override fun onTouchEvent(e: MotionEvent?): Boolean {
    e ?: return super.onTouchEvent(e)
    if (directionLockEnabled) lockHelper.lock(e)
    return super.onTouchEvent(e)
  }

  override fun fling(
    velocityX: Int,
    velocityY: Int
  ): Boolean {
    if (!directionLockEnabled) return super.fling(velocityX, velocityY)
    val fixedVelocityX = if (lockHelper.isLockVertical()) 0 else velocityX
    val fixedVelocityY = if (lockHelper.isLockHorizontal()) 0 else velocityY
    return super.fling(fixedVelocityX, fixedVelocityY)
  }

}
