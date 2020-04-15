package com.tonyjhuang.qfit.ui.viewgroup

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tonyjhuang.qfit.ui.viewgroup.GroupGoalPageFragment.Companion.ARG_GROUP_GOAL_ID

class GroupGoalFragmentStateAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment) {

    private var goalIds: List<String> = emptyList()

    override fun getItemCount() = goalIds.size

    override fun createFragment(position: Int): Fragment{
        val fragment = GroupGoalPageFragment()
        fragment.arguments = Bundle().apply {
            putString(ARG_GROUP_GOAL_ID, goalIds[position])
        }
        return fragment
    }

    fun setStates(goalIds: List<String>) {
        this.goalIds = goalIds
        notifyDataSetChanged()
    }
}