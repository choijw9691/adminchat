package com.project.adminchat.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.project.adminchat.databinding.ItemGymBinding
import com.project.adminchat.model.Gym

class GymAdapter(val onClick: (Gym) ->Unit) : RecyclerView.Adapter<GymAdapter.GymViewHolder>() {
    private var gymList: List<Gym> = listOf()

    inner class GymViewHolder(val binding: ItemGymBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(gym: Gym) {
            binding.nameTextView.text = gym.name
            itemView.setOnClickListener {
                onClick(gym)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GymViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemGymBinding.inflate(inflater, parent, false)
        return GymViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GymViewHolder, position: Int) {
        val gym = gymList[position]
        holder.bind(gym)
    }

    override fun getItemCount(): Int {
        return gymList.size
    }

    fun submitList(newList: List<Gym>) {
        gymList = newList
        notifyDataSetChanged()
    }
}