package cn.jingzhuan.tableview.directionlock

import android.graphics.Canvas
import androidx.recyclerview.widget.RecyclerView
import android.widget.EdgeEffect

internal class DirectionLockRecyclerViewEdgeEffect(private val view: RecyclerView, @RecyclerView.EdgeEffectFactory.EdgeDirection private val direction: Int) :
    EdgeEffect(view.context) {

  override fun onPull(deltaDistance: Float) {
    if (isEnabled()) super.onPull(deltaDistance)
  }

  override fun onPull(
    deltaDistance: Float,
    displacement: Float
  ) {
    if (isEnabled()) super.onPull(deltaDistance, displacement)
  }

  override fun onAbsorb(velocity: Int) {
    if (isEnabled()) super.onAbsorb(velocity)
  }

  override fun isFinished(): Boolean {
    return if (isEnabled()) true else super.isFinished()
  }

  override fun draw(canvas: Canvas?): Boolean {
    return if (isEnabled()) super.draw(canvas) else false
  }

  private fun isEnabled(): Boolean {
    if (view !is DirectionLockRecyclerView) return true
    var enabled = true
    if (!view.enableHorizontalGlow && (direction == RecyclerView.EdgeEffectFactory.DIRECTION_RIGHT || direction == RecyclerView.EdgeEffectFactory.DIRECTION_LEFT)) {
      enabled = false
    }
    if (!view.enableVerticalGlow && (direction == RecyclerView.EdgeEffectFactory.DIRECTION_TOP || direction == RecyclerView.EdgeEffectFactory.DIRECTION_BOTTOM)) {
      enabled = false
    }
    return enabled
  }

}