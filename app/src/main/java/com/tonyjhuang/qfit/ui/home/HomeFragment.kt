package com.tonyjhuang.qfit.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.tonyjhuang.qfit.QLog
import com.tonyjhuang.qfit.R
import com.tonyjhuang.qfit.data.*
import kotlinx.android.synthetic.main.fragment_home.*


class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var adapter: DailyUserProgressRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val userRepository = UserRepository(Firebase.database.reference)
        val currentUserRepository = CurrentUserRepository(userRepository)
        val groupRepository = GroupRepository(Firebase.database.reference, userRepository)
        val goalRepository = GoalRepository(Firebase.database.reference)
        val progressRepository = UserProgressRepository(Firebase.database.reference)
        homeViewModel =
            ViewModelProviders.of(
                this,
                HomeViewModelFactory(
                    groupRepository,
                    currentUserRepository,
                    goalRepository,
                    progressRepository,
                    userRepository
                )
            ).get(HomeViewModel::class.java)
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeViewModel.header.observe(viewLifecycleOwner, Observer {
            header.text = it
        })
        adapter = DailyUserProgressRecyclerViewAdapter(requireContext()) { goalId, updateAmount ->
            QLog.d("hello? $goalId $updateAmount")
            homeViewModel.updateUserProgress(goalId, updateAmount)
            // Hide keyboard
            view.let { v ->
                val imm =
                    context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(v.windowToken, 0)
            }
        }

        with(progress_list) {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            adapter = this@HomeFragment.adapter
        }
        homeViewModel.dailyUserProgress.observe(viewLifecycleOwner, Observer {
            adapter.dailyUserProgress = it
            adapter.notifyDataSetChanged()
        })

    }
}
