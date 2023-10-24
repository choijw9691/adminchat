package com.project.adminchat.ui.notifications

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.project.adminchat.MainActivity
import com.project.adminchat.R
import com.project.adminchat.common.ConfirmationDialog
import com.project.adminchat.databinding.FragmentNotificationsBinding
import com.project.adminchat.ui.adapter.IconAdapter
import com.project.adminchat.ui.home.MainViewModel
import com.project.adminchat.ui.login.LoginActivity

class NotificationsFragment : Fragment() {

    lateinit var binding: FragmentNotificationsBinding
    private val mainViewModel: MainViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentNotificationsBinding.inflate(inflater, container, false)

        binding.myProfile.nameTv.visibility = View.GONE
        binding.myProfile.todayWorkoutTv.visibility = View.GONE
        binding.editBtn.setOnClickListener {
            binding.nicknameEv.isEnabled = true
            binding.todayWorkoutEv.isEnabled = true
            binding.editBtn.visibility = View.GONE
            binding.completeBtn.visibility = View.VISIBLE
            binding.myProfile.editImg.visibility = View.VISIBLE

        }
        binding.completeBtn.setOnClickListener {
            MainActivity.myProfile?.nickname = binding.nicknameEv.text.toString()
            MainActivity.myProfile?.todayWorkOut = binding.todayWorkoutEv.text.toString()
            MainActivity.myProfile?.let { it1 ->
                mainViewModel.updateUserWithGym(it1){ isSuccess ->
                    if (isSuccess) {
                        // 업데이트 성공
                        Toast.makeText(context, "프로필이 수정되었습니다.", Toast.LENGTH_SHORT).show()
                        binding.nicknameEv.isEnabled = false
                        binding.todayWorkoutEv.isEnabled = false
                        binding.editBtn.visibility = View.VISIBLE
                        binding.completeBtn.visibility = View.GONE
                        binding.myProfile.editImg.visibility = View.GONE
                    } else {
                        // 업데이트 실패
                    }
                }
            }
        }
        binding.logoutBtn.setOnClickListener {
            ConfirmationDialog().show(childFragmentManager,"정말로 로그아웃 하시겠습니까?",{
                val firebaseAuth = FirebaseAuth.getInstance()
                firebaseAuth.signOut()
                var intent = Intent(context,LoginActivity::class.java)
                startActivity(intent)
                (activity as MainActivity).finish()
            },{})
        }
        binding.myProfile.editImg.setOnClickListener {
            showIconDialog(requireContext()) {
                it?.let { setImage(it)
                MainActivity.myProfile?.profile_image = it
                }
            }
        }
        var imageNum = MainActivity.myProfile?.profile_image
        imageNum?.let { setImage(it) }
        binding.nicknameEv.setText(MainActivity.myProfile?.nickname)
        binding.todayWorkoutEv.setText(MainActivity.myProfile?.todayWorkOut)
        return binding.root
    }

    fun showIconDialog(context: Context, callback: (position: Int) -> Unit) {
        var dialog: AlertDialog? = null
        val icons = listOf(
            R.drawable.icon1, R.drawable.icon2, R.drawable.icon3,
            R.drawable.icon4, R.drawable.icon5, R.drawable.icon6,
            R.drawable.icon7, R.drawable.icon8, R.drawable.icon9,
            R.drawable.icon10
        )
        val recyclerView = RecyclerView(context)
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = IconAdapter(icons, {
            dialog?.dismiss()
        }, callback)

        val builder = AlertDialog.Builder(context)
        builder.setView(recyclerView)
        dialog = builder.create()
        dialog.show()
    }

    fun setImage(imageNum: Int) {
        var imgResource: Int = when (imageNum) {
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
        binding.myProfile.profileIv.setImageResource(imgResource)

    }
}