package cn.jingzhuan.tableview.demo.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.jingzhuan.tableview.demo.R

class MainAdapter : RecyclerView.Adapter<MainItemViewHolder>() {

    val data = mutableListOf<String>()
    var onItemClick: ((Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_main, parent, false)
        return MainItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: MainItemViewHolder, position: Int) {
        holder.onBind(data[position])
        holder.itemView.setOnClickListener { onItemClick?.invoke(position) }
    }

    override fun getItemCount(): Int {
        return data.size
    }

}