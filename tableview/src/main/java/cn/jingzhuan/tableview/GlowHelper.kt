package cn.jingzhuan.tableview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Point
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.core.widget.EdgeEffectCompat
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.View
import android.widget.EdgeEffect
import kotlin.LazyThreadSafetyMode.NONE

/**
 * Chenyikang
 * 2019 January 09
 */
internal class GlowHelper(
  private val target: View,
  private val context: Context = target.context
) {

  var glowColor: Int? = null
  var glowAlpha = 0F
  private var leftGlow: EdgeEffect? = null
  private var topGlow: EdgeEffect? = null
  private var rightGlow: EdgeEffect? = null
  private var bottomGlow: EdgeEffect? = null
  private val touchPoint by lazy(NONE) { Point() }

  fun draw(canvas: Canvas) {
    var needInvalidate = false
    needInvalidate = drawGlow(leftGlow, canvas, 270F) || needInvalidate
    needInvalidate = drawGlow(topGlow, canvas, 0F) || needInvalidate
    needInvalidate = drawGlow(rightGlow, canvas, 90F) || needInvalidate
    needInvalidate = drawGlow(bottomGlow, canvas, 180F) || needInvalidate
    if (needInvalidate) {
      ViewCompat.postInvalidateOnAnimation(target)
    }
  }

  fun onInterceptTouchEvent(ev: MotionEvent) {
    when (ev.actionMasked) {
      ACTION_DOWN -> touchPoint.set(ev.rawX.toInt(), ev.rawY.toInt())
    }
  }

  fun onSizeChanged() {
    leftGlow = null
    topGlow = null
    rightGlow = null
    bottomGlow = null
  }

  fun onNestedScroll(
    dxConsumed: Int,
    dyConsumed: Int,
    dxUnconsumed: Int,
    dyUnconsumed: Int
  ) {
    touchPoint.x -= dxConsumed + dxUnconsumed
    touchPoint.y -= dyConsumed + dyUnconsumed
    if (dxUnconsumed < 0) {
      ensureLeftGlow()
      EdgeEffectCompat.onPull(
          leftGlow!!, -dxUnconsumed / target.measuredWidth.toFloat(), 1 - touchPoint.y / target.measuredHeight.toFloat()
      )
    } else {
      ensureRightGlow()
      EdgeEffectCompat.onPull(
          rightGlow!!, dxUnconsumed / target.measuredWidth.toFloat(), touchPoint.y / target.measuredHeight.toFloat()
      )
    }

    if (dyUnconsumed < 0) {
      ensureTopGlow()
      EdgeEffectCompat.onPull(
          topGlow!!, -dyUnconsumed / target.measuredHeight.toFloat(), touchPoint.x / target.measuredWidth.toFloat()
      )
    } else {
      ensureBottomGlow()
      EdgeEffectCompat.onPull(
          bottomGlow!!, dyUnconsumed / target.measuredHeight.toFloat(), 1 - touchPoint.x / target.measuredWidth.toFloat()
      )
    }
    ViewCompat.postInvalidateOnAnimation(target)
  }

  fun onStopNestedScroll() {
    leftGlow?.onRelease()
    topGlow?.onRelease()
    rightGlow?.onRelease()
    bottomGlow?.onRelease()
    ViewCompat.postInvalidateOnAnimation(target)
  }

  private fun drawGlow(
    edgeEffect: EdgeEffect?,
    canvas: Canvas,
    degree: Float
  ): Boolean {
    edgeEffect?.let {
      if (!it.isFinished) {
        val restore = canvas.save()
        canvas.rotate(degree)
        when (degree) {
          90F -> canvas.translate(0F, -target.measuredWidth.toFloat())
          180F -> canvas.translate(
              -target.measuredWidth.toFloat(), -target.measuredHeight.toFloat()
          )
          270F -> canvas.translate(-target.measuredHeight.toFloat(), 0F)
        }
        it.draw(canvas)
        canvas.restoreToCount(restore)
        return true
      }
    }
    return false
  }

  private fun ensureLeftGlow() {
    if (null == leftGlow) {
      leftGlow = EdgeEffect(context)
      glowColor?.let {
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
          leftGlow!!.color = ColorUtils.setAlphaComponent(it, ((1 - glowAlpha) * 255).toInt())
        }
      }
      leftGlow!!.setSize(target.measuredHeight, target.measuredWidth)
    }
  }

  private fun ensureTopGlow() =
    topGlow
        ?: newGlow(glowColor, glowAlpha, target.measuredWidth, target.measuredHeight)
            .apply { topGlow = this }

  private fun ensureRightGlow() =
    rightGlow
        ?: newGlow(glowColor, glowAlpha, target.measuredHeight, target.measuredWidth)
            .apply { rightGlow = this }

  private fun ensureBottomGlow() =
    bottomGlow
        ?: newGlow(glowColor, glowAlpha, target.measuredWidth, target.measuredHeight)
            .apply { bottomGlow = this }

  private fun newGlow(
    glowColor: Int?,
    glowAlpha: Float,
    width: Int,
    height: Int
  ): EdgeEffect {
    val glow = EdgeEffect(context)
    glowColor?.apply {
      if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
        glow.color = ColorUtils.setAlphaComponent(this, ((1 - glowAlpha) * 255).toInt())
      }
    }
    glow.setSize(width, height)
    return glow
  }

}