package com.tonyjhuang.qfit.ui.viewgroup

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.tonyjhuang.qfit.R
import com.tonyjhuang.qfit.data.*
import com.tonyjhuang.qfit.ui.Konfetti
import kotlinx.android.synthetic.main.fragment_view_group.*
import kotlinx.android.synthetic.main.fragment_view_group.view.*

class ViewGroupFragment : Fragment() {

    lateinit var viewModel: ViewGroupViewModel

    private lateinit var pagerAdapter: GroupGoalFragmentStateAdapter
    private lateinit var userPhotosAdapter: UserPhotosRecyclerViewAdapter

    private lateinit var groupId: String

    override fun onAttach(context: Context) {
        super.onAttach(context)

        groupId = requireArguments().let {
            ViewGroupFragmentArgs.fromBundle(
                it
            ).groupId
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val userRepository = UserRepository(Firebase.database.reference)
        val groupRepository = GroupRepository(Firebase.database.reference, userRepository)
        val goalRepository = GoalRepository(Firebase.database.reference)
        val progressRepository = ProgressRepository(
            Firebase.database.reference,
            userRepository,
            groupRepository
        )
        val currentUserRepository = CurrentUserRepository(userRepository)
        viewModel =
            ViewModelProviders.of(
                this,
                ViewGroupViewModelFactory(
                    groupRepository,
                    userRepository,
                    goalRepository,
                    progressRepository,
                    currentUserRepository
                )
            ).get(ViewGroupViewModel::class.java)

        pagerAdapter = GroupGoalFragmentStateAdapter(this)
        userPhotosAdapter = UserPhotosRecyclerViewAdapter(requireContext())

        return inflater.inflate(R.layout.fragment_view_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.overflow.setOnClickListener { showOverflow(view.overflow) }

        with(view.pager) {
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
        with(view.user_photos) {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = userPhotosAdapter
        }

        viewModel.groupRequested(groupId)

        viewModel.groupName.observe(viewLifecycleOwner, Observer {
            group_name.text = it
        })
        viewModel.groupMembers.observe(viewLifecycleOwner, Observer {
            member_count.text = it.size.toString() + " members"
            userPhotosAdapter.users = it
        })

        viewModel.groupGoalProgress.observe(viewLifecycleOwner, Observer {
            pagerAdapter.setStates(it.keys.toList())
        })

        viewModel.events.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ViewGroupViewModel.Event.AchievedNewGoalEvent -> Konfetti.show(view.konfetti)
                is ViewGroupViewModel.Event.LeftGroupEvent -> {
                    showToast("Left group")
                    findNavController().navigate(R.id.action_view_group_to_group_list)
                }
                is ViewGroupViewModel.Event.GroupDeletedEvent -> {
                    showToast("Group deleted")
                    findNavController().navigate(R.id.action_view_group_to_group_list)
                }
                is ViewGroupViewModel.Event.GroupDisbandedEvent -> {
                    showToast("Group was disbanded")
                    findNavController().navigate(R.id.action_view_group_to_group_list)
                }
            }
        })
    }

    private fun showToast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    private fun showOverflow(view: View) {
        val popup = PopupMenu(requireContext(), view).apply {
            setOnMenuItemClickListener {
                when(it.itemId) {
                    R.id.delete_group -> {
                        viewModel.deleteGroup()
                        true
                    }
                    R.id.leave_group -> {
                        viewModel.leaveGroup()
                        true
                    }
                    else -> false
                }
            }
        }
        val menuLayout = if(viewModel.isCurrentUserAdmin) R.menu.view_group_admin_menu else  R.menu.view_group_member_menu
        popup.menuInflater.inflate(menuLayout, popup.menu)
        popup.show()
    }
}