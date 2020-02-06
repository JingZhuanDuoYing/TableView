package cn.jingzhuan.tableview.directionlock

import android.support.v7.widget.RecyclerView
import android.widget.EdgeEffect

internal class DirectionLockRecyclerViewEdgeEffectFactory : RecyclerView.EdgeEffectFactory() {

  override fun createEdgeEffect(
    view: RecyclerView,
    direction: Int
  ): EdgeEffect {
    return DirectionLockRecyclerViewEdgeEffect(
        view, direction
    )
  }

}