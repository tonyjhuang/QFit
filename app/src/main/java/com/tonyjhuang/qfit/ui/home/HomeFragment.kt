package com.tonyjhuang.qfit.ui.home

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.tonyjhuang.qfit.R
import com.tonyjhuang.qfit.data.*
import com.tonyjhuang.qfit.ui.Konfetti
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*


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
        val progressRepository = ProgressRepository(
            Firebase.database.reference,
            userRepository,
            groupRepository
        )
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
            view.header.text = it
        })
        homeViewModel.subheader.observe(viewLifecycleOwner, Observer {
            view.subtext.text = it
        })
        homeViewModel.userPhoto.observe(viewLifecycleOwner, Observer {
            Glide.with(requireContext())
                .load(it)
                .into(view.user_photo)
        })
        adapter = DailyUserProgressRecyclerViewAdapter(object : DailyUserProgressRecyclerViewAdapter.Listener {
            override fun initiateProgressUpdate(goalId: String) {
                getNewUserProgress {
                    val newProgress = it.toIntOrNull() ?: 0
                    if (newProgress != 0) {
                        homeViewModel.updateUserProgress(goalId, newProgress)
                    }
                }
            }

            override fun onGroupClicked(groupId: String) {
                findNavController().navigate(HomeFragmentDirections.actionHomeToViewGroup(groupId))
            }
        })

        with(progress_list) {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            adapter = this@HomeFragment.adapter
        }
        homeViewModel.dailyUserProgress.observe(viewLifecycleOwner, Observer {
            adapter.dailyUserProgress = it
            adapter.notifyDataSetChanged()
            if (it.isEmpty()) {
                view.empty_state.visibility = View.VISIBLE
            } else {
                view.empty_state.visibility = View.INVISIBLE
            }
        })
        homeViewModel.events.observe(viewLifecycleOwner, Observer {
            when (it) {
                is HomeViewModel.Event.AchievedNewGoalEvent -> Konfetti.show(view.konfetti)
            }
        })
    }

    private fun getNewUserProgress(callback: (String) -> Unit) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        val view: View? =
            LayoutInflater.from(context)?.inflate(R.layout.dialog_get_new_progress, null)
        dialogBuilder
            .setView(view)
            .setCancelable(false)
            .setPositiveButton("Record", DialogInterface.OnClickListener { _, _ ->
                callback(view?.findViewById<EditText>(R.id.progress_amount)?.text.toString())
            })
            .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, _ ->
                dialog.cancel()
            })
            .create()
            .show()
    }
}
