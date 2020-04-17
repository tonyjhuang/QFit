package com.tonyjhuang.qfit.ui.viewgroup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.tonyjhuang.qfit.R
import com.tonyjhuang.qfit.data.GoalRepository
import com.tonyjhuang.qfit.data.GroupRepository
import com.tonyjhuang.qfit.data.UserRepository
import kotlinx.android.synthetic.main.fragment_view_group.*

class ViewGroupFragment : Fragment() {

    lateinit var viewModel: ViewGroupViewModel

    private lateinit var pagerAdapter: GroupGoalFragmentStateAdapter

    private lateinit var groupId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val userRepository = UserRepository(Firebase.database.reference)
        val groupRepository = GroupRepository(Firebase.database.reference, userRepository)
        val goalRepository = GoalRepository(Firebase.database.reference)
        viewModel =
            ViewModelProviders.of(
                this,
                ViewGroupViewModelFactory(
                    groupRepository,
                    userRepository,
                    goalRepository
                )
            ).get(ViewGroupViewModel::class.java)

        pagerAdapter = GroupGoalFragmentStateAdapter(this)

        return inflater.inflate(R.layout.fragment_view_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        groupId = requireArguments().let {
            ViewGroupFragmentArgs.fromBundle(
                it
            ).groupId
        }
        viewModel.groupRequested(groupId)

        viewModel.groupName.observe(viewLifecycleOwner, Observer {
            group_name.text = it
        })
        viewModel.totalMembers.observe(viewLifecycleOwner, Observer {
            member_count.text = it.toString() + " members"
        })

        viewModel.groupGoals.observe(viewLifecycleOwner, Observer {
            pagerAdapter.setStates(it.keys.toList())
        })

        with(pager) {
            adapter = pagerAdapter
            offscreenPageLimit = 3

            // taken from https://proandroiddev.com/look-deep-into-viewpager2-13eb8e06e419
            val pageMarginPx = resources.getDimensionPixelOffset(R.dimen.pageMargin)
            val offsetPx = resources.getDimensionPixelOffset(R.dimen.pageOffset)
            setPageTransformer { page, position ->

                val offset = position * -(2 * offsetPx + pageMarginPx)
                if (this.orientation == ViewPager2.ORIENTATION_HORIZONTAL) {
                    if (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                        page.translationX = -offset
                    } else {
                        page.translationX = offset
                    }
                } else {
                    page.translationY = offset
                }
            }
        }
    }
}