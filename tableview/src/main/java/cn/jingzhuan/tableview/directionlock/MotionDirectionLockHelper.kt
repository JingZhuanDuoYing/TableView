package cn.jingzhuan.tableview.directionlock

import android.graphics.PointF
import android.view.MotionEvent
import kotlin.math.abs

class MotionDirectionLockHelper(
  var lockHorizontalOnly: Boolean = false,
  var lockVerticalOnly: Boolean = false,
  var touchSlop: Int = 0
) {

  private val downPoint = PointF()
  private val lastPoint = PointF()
  private val directionLockIdle = 0
  private val directionLockHorizontal = 1
  private val directionLockVertical = 2
  private var directionLock = directionLockIdle

  fun lock(event: MotionEvent) {
    when (event.actionMasked) {
      MotionEvent.ACTION_DOWN -> {
        directionLock = directionLockIdle
        downPoint.set(event.x, event.y)
      }
      else -> {
        if (directionLock == directionLockIdle) {
          changeDirectionLock(event, downPoint)
        }
        modifyMotionEventLocation(event, lastPoint)
      }
    }
    lastPoint.set(event.x, event.y)
  }

  fun isLockHorizontal() = directionLock == directionLockHorizontal

  fun isLockVertical() = directionLock == directionLockVertical

  private fun changeDirectionLock(
    e: MotionEvent,
    downPoint: PointF
  ) {
    val deltaX = abs(e.x - downPoint.x)
    val deltaY = abs(e.y - downPoint.y)
    if (deltaX < touchSlop && deltaY < touchSlop) return

    directionLock = when {
      deltaX > deltaY && !lockVerticalOnly -> directionLockHorizontal
      deltaY > deltaX && !lockHorizontalOnly -> directionLockVertical
      else -> directionLockIdle
    }
  }

  private fun modifyMotionEventLocation(
    e: MotionEvent,
    lastPoint: PointF
  ) {
    when (directionLock) {
      directionLockHorizontal -> {
        if (!lockVerticalOnly) e.setLocation(e.x, lastPoint.y)
      }
      directionLockVertical -> {
        if (!lockHorizontalOnly) e.setLocation(lastPoint.x, e.y)
      }
    }
  }

}