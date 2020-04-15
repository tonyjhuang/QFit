package com.tonyjhuang.qfit.ui.viewgroup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.tonyjhuang.qfit.QLog
import com.tonyjhuang.qfit.R
import kotlinx.android.synthetic.main.content_group_goal.*

class GroupGoalPageFragment : Fragment() {

    private lateinit var viewModel: ViewGroupViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.content_group_goal, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Ugly hack :P
        viewModel = (requireParentFragment() as ViewGroupFragment).viewModel
        val goalId = requireArguments().getString(ARG_GROUP_GOAL_ID)!!
        goal_name.text = goalId

        viewModel.groupGoals.observe(viewLifecycleOwner, Observer {
            val goal: GroupGoalState = it[goalId]!!
            goal_name.text = "${goal.amount} ${goal.name}"
        })
    }

    companion object {
        const val ARG_GROUP_GOAL_ID = "goal_id"
    }
}