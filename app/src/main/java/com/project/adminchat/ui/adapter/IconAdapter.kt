package com.project.adminchat.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.project.adminchat.R

class IconAdapter(private val icons: List<Int>, private val dismissCallback: ()->Unit, private val callback: (position: Int) -> Unit) : RecyclerView.Adapter<IconAdapter.IconViewHolder>() {

    inner class IconViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconView: ImageView = itemView.findViewById(R.id.iconImageView)

        init {
            itemView.setOnClickListener {
                callback(adapterPosition+1)
                dismissCallback()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.icon_item, parent, false)
        return IconViewHolder(view)
    }

    override fun getItemCount(): Int = icons.size

    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        holder.iconView.setImageResource(icons[position])
    }
}
