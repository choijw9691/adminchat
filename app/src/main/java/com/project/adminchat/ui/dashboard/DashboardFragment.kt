package com.project.adminchat.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.project.adminchat.MainActivity
import com.project.adminchat.R
import com.project.adminchat.common.ConfirmationDialog
import com.project.adminchat.common.Constants.SEND_MESSAGE
import com.project.adminchat.common.repeatCollectOnStarted
import com.project.adminchat.databinding.FragmentDashboardBinding
import com.project.adminchat.model.Gym
import com.project.adminchat.model.UserEntity
import com.project.adminchat.ui.adapter.DashboardAdapter
import com.project.adminchat.ui.home.MainViewModel
import com.project.adminchat.ui.util.SendMessageDialog
import kotlinx.coroutines.flow.collectLatest

class DashboardFragment : Fragment() {

 var  listenerRegistration: ListenerRegistration? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    lateinit var  binding :FragmentDashboardBinding
    val dialog = ConfirmationDialog()
    private val mainViewModel: MainViewModel by activityViewModels()
    var gymAdapter = DashboardAdapter{
        MainActivity.myProfile?.let { it1 ->
            Log.d("JIWOUNG","fnklewm4lm "+it.token+"||"+it1.token)
            SendMessageDialog("",SEND_MESSAGE.toString(),binding.gymNameTv.text.toString(),
                it1.nickname,"",it.token,it1.token).show(parentFragmentManager,"")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root
        if (MainActivity.myProfile?.currentGym.isNullOrEmpty()){
            binding.emptyContainer.visibility = View.VISIBLE
            binding.container.visibility = View.GONE
        }else{
            binding.emptyContainer.visibility = View.GONE
            binding.container.visibility = View.VISIBLE
            initView()
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
    override fun onDestroyView() {
        super.onDestroyView()
    }

    fun initView(){

        MainActivity.myProfile?.profile_image?.let { setImage(it) }
        binding.myProfile.nameTv.text = MainActivity.myProfile?.nickname
        binding.myProfile.todayWorkoutTv.text = MainActivity.myProfile?.todayWorkOut
        binding.dashboardRv.apply {
            adapter = gymAdapter
            layoutManager = LinearLayoutManager(context)
        }
        binding.exitGym.setOnClickListener{
            mainViewModel.exitCurrentGym()
            listenerRegistration?.remove()
            findNavController().popBackStack()
        }
        repeatCollectOnStarted {
            mainViewModel.currentGym.collectLatest {
                if (it != null) {
                    binding.gymNameTv.text = it.name
                    binding.gymMemberSizeTv.text= "인원(${it.userList.size-1})"
                    gymAdapter.submitList(it.userList.filter { it != MainActivity.myProfile })
                }
            }
        }
        getChangedListener()
    }
fun getChangedListener(){
    val db = FirebaseFirestore.getInstance()
    val docRef = db.collection("gyms").document(mainViewModel.currentGym.value?.documentId.toString())
    listenerRegistration = docRef.addSnapshotListener { snapshot, e ->
        if (e != null) {
            Log.d("JIWOUNG", "UpdateduserList0000:")

            return@addSnapshotListener
        }

        if (snapshot != null && snapshot.exists()) {
            // userList 변경이 감지되었습니다.
            val gym = snapshot.toObject(Gym::class.java)
            val userList = gym?.userList as? List<UserEntity> // 리스트의 타입에 따라 캐스팅을 변경해주시면 됩니다.

            userList?.let {
                if (it.contains(MainActivity.myProfile)){
                    Log.d("JIWOUNG", "UpdateduserList1111:")

                    binding.gymMemberSizeTv.text= "인원(${userList.size-1})"
                    gymAdapter.submitList(userList.filter { it != MainActivity.myProfile })
                }
            }

        }
    }

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
    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration?.remove()
    }
}