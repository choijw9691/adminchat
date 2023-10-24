package com.project.adminchat.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.adminchat.MainActivity
import com.project.adminchat.R
import com.project.adminchat.databinding.FragmentHomeBinding
import com.project.adminchat.model.Gym
import com.project.adminchat.ui.adapter.GymAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    var gymAdapter = GymAdapter{
        if (MainActivity.myProfile?.currentGym.isNullOrEmpty()){
            mainViewModel.setUpdateCurrentGym(it)
            findNavController().navigate(R.id.action_home_to_dashboard)
        }else{
            Toast.makeText(context, "참여중인 헬스장이 있습니다.", Toast.LENGTH_SHORT).show()
        }
    }
    private val mainViewModel: MainViewModel by activityViewModels()
    var gymList :List<Gym>? = listOf()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        initSearchView()

        // 리사이클러뷰 설정
        binding.homeRv.apply {
            adapter = gymAdapter
            layoutManager = LinearLayoutManager(context)
        }
        mainViewModel.getAllGymsFromFirestore()
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        CoroutineScope(Dispatchers.IO).launch {
            mainViewModel.gymList.collectLatest {
                if (it != null) {
                        gymList = it
                    } else {
                        gymList = listOf()
                    }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun initSearchView() {
        binding.searchView.setIconifiedByDefault(false)
        binding.searchView.queryHint = "헬스장을 검색해 주세요."
        binding.searchView.setOnClickListener {
            binding.searchView.isIconified = false
        }


        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // 검색 버튼을 눌렀을 때 처리
                if (!query.isNullOrEmpty()) {
                    // 검색어를 이용하여 원하는 동작 수행
                    performSearch(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // 검색어가 변경될 때 처리 (실시간 검색 등)
                if (!newText.isNullOrEmpty()) {
                    // 검색어를 이용하여 원하는 동작 수행
                    // performSearch(newText)
                }
                return true
            }
        })

    }

    private fun performSearch(query: String) {
        gymList?.let { gymAdapter.submitList(it.filter { gym -> gym.name.contains(query, ignoreCase = true) }) }

    }


}