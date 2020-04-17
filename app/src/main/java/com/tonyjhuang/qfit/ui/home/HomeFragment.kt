package com.tonyjhuang.qfit.ui.home

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.tonyjhuang.qfit.R
import com.tonyjhuang.qfit.data.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import nl.dionsegijn.konfetti.KonfettiView
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size


class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var adapter: DailyUserProgressRecyclerViewAdapter
    private lateinit var konfetti: KonfettiView

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
        konfetti = view.konfetti

        homeViewModel.header.observe(viewLifecycleOwner, Observer {
            header.text = it
        })
        adapter = DailyUserProgressRecyclerViewAdapter { goalId ->
            getNewUserProgress {
                val newProgress = it.toIntOrNull() ?: 0
                if (newProgress != 0) {
                    homeViewModel.updateUserProgress(goalId, newProgress)
                }
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
        homeViewModel.events.observe(viewLifecycleOwner, Observer {
            when (it) {
                is HomeViewModel.Event.AchievedNewGoalEvent -> {
                    showConfetti()
                }
            }
        })
    }

    private fun showConfetti() {
        konfetti.build()
            .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA)
            .setDirection(0.0, 359.0)
            .setSpeed(6f, 12f)
            .setFadeOutEnabled(true)
            .setTimeToLive(1000L)
            .addShapes(Shape.Square, Shape.Circle)
            .addSizes(Size(12), Size(16, 6f))
            .setPosition(konfetti.x + konfetti.width / 2, konfetti.y + konfetti.height / 3)
            .burst(100)
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
