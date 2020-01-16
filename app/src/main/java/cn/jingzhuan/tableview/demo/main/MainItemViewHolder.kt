package cn.jingzhuan.tableview.demo.main

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MainItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    fun onBind(text: String) {
        (itemView as? TextView)?.text = text
    }

}