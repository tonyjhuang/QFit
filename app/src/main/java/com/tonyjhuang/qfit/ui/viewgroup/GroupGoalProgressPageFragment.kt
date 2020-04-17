package com.tonyjhuang.qfit.ui.viewgroup

import android.content.DialogInterface
import android.graphics.Outline
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.tonyjhuang.qfit.R
import kotlinx.android.synthetic.main.fragment_group_goal_progress.*
import kotlinx.android.synthetic.main.fragment_group_goal_progress.view.*

class GroupGoalProgressPageFragment : Fragment() {

    private lateinit var viewModel: ViewGroupViewModel
    private lateinit var profile: ImageView
    private lateinit var adapter: DailyUserProgressRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_group_goal_progress, container, false)
        // Circular shadow, this took me WAY too much time.
        view.leaderboard.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View?, outline: Outline?) {
                val rect = Rect()
                view?.background?.copyBounds(rect)
                rect.offset(0, 0)

                val cornerRadius  = (view?.width?.toFloat() ?: 0f) / 2
                outline?.setRoundRect(rect, cornerRadius)
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Ugly hack :P
        viewModel = (requireParentFragment() as ViewGroupFragment).viewModel
        val goalId = requireArguments().getString(ARG_GROUP_GOAL_ID)!!
        goal_name.text = goalId

        adapter = DailyUserProgressRecyclerViewAdapter(requireContext())
        with (view.user_progress_recyclerview) {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@GroupGoalProgressPageFragment.adapter
        }

        viewModel.groupGoalProgress.observe(viewLifecycleOwner, Observer {
            val progressView: GroupGoalProgressView = it[goalId]!!
            goal_name.text = "${progressView.goalAmount} ${progressView.goalName}"
            adapter.userProgress = progressView.userProgress
        })

        view.initiate_progress_update.setOnClickListener {
            getNewUserProgress {
                val newProgress = it.toIntOrNull() ?: 0
                if (newProgress != 0) {
                    viewModel.updateUserProgress(goalId, newProgress)
                }
            }
        }
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

    companion object {
        const val ARG_GROUP_GOAL_ID = "goal_id"
    }
}