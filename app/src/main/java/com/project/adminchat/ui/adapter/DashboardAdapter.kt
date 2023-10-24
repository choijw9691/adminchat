package com.project.adminchat.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.project.adminchat.R
import com.project.adminchat.databinding.ItemUserBinding
import com.project.adminchat.model.UserEntity

class DashboardAdapter (val onClick: (UserEntity) ->Unit) : RecyclerView.Adapter<DashboardAdapter.ProfileHolder>() {
    private var userList: List<UserEntity> = listOf()

    inner class ProfileHolder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: UserEntity) {

            var imgResource: Int = when (user.profile_image) {
                1 -> {
                    R.drawable.icon1
                }

                2 -> {
                    R.drawable.icon2
                }

                3 -> {
                    R.drawable.icon3
                }

                4 -> {
                    R.drawable.icon4
                }

                5 -> {
                    R.drawable.icon5
                }

                6 -> {
                    R.drawable.icon6
                }

                7 -> {
                    R.drawable.icon7
                }

                8 -> {
                    R.drawable.icon8
                }

                9 -> {
                    R.drawable.icon9
                }

                10 -> {
                    R.drawable.icon10
                }

                else -> {
                    R.drawable.icon1
                }
            }

            binding.profileIv.setImageResource(imgResource)
            binding.todayWorkoutTv.text = user.todayWorkOut
            binding.nicknameTv.text = user.nickname
            itemView.setOnClickListener {
                onClick(user)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemUserBinding.inflate(inflater, parent, false)
        return ProfileHolder(binding)
    }

    override fun onBindViewHolder(holder: ProfileHolder, position: Int) {
        val user = userList[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    fun submitList(newList: List<UserEntity>) {
        userList = newList
        notifyDataSetChanged()
    }

}