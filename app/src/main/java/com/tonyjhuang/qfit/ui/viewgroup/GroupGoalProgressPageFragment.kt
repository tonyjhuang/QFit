package com.tonyjhuang.qfit.ui.viewgroup

import android.graphics.Outline
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.tonyjhuang.qfit.R
import kotlinx.android.synthetic.main.fragment_group_goal_progress.*
import kotlinx.android.synthetic.main.fragment_group_goal_progress.view.*

class GroupGoalProgressPageFragment : Fragment() {

    private lateinit var viewModel: ViewGroupViewModel
    private lateinit var profile: ImageView

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

        viewModel.groupGoalProgress.observe(viewLifecycleOwner, Observer {
            val progressView: GroupGoalProgressView = it[goalId]!!
            goal_name.text = "${progressView.goalAmount} ${progressView.goalName}"
        })

        view
    }

    companion object {
        const val ARG_GROUP_GOAL_ID = "goal_id"
    }
}