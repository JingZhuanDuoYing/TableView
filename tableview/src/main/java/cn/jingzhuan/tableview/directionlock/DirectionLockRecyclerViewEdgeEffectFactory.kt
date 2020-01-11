package cn.jingzhuan.tableview.directionlock

import android.widget.EdgeEffect
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.EdgeEffectFactory

internal class DirectionLockRecyclerViewEdgeEffectFactory : EdgeEffectFactory() {

  override fun createEdgeEffect(
    view: RecyclerView,
    direction: Int
  ): EdgeEffect {
    return DirectionLockRecyclerViewEdgeEffect(
        view, direction
    )
  }

}