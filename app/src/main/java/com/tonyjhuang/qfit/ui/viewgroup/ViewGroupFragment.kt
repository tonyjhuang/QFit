package com.tonyjhuang.qfit.ui.viewgroup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.tonyjhuang.qfit.R
import com.tonyjhuang.qfit.data.GroupRepository
import com.tonyjhuang.qfit.data.UserRepository
import com.tonyjhuang.qfit.ui.groups.ViewGroupFragmentArgs
import kotlinx.android.synthetic.main.fragment_view_group.*

class ViewGroupFragment : Fragment() {

    private lateinit var viewModel: ViewGroupViewModel

    private lateinit var groupId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val userRepository = UserRepository(Firebase.database.reference)
        val groupRepository = GroupRepository(Firebase.database.reference, userRepository)
        viewModel =
            ViewModelProviders.of(
                this,
                ViewGroupViewModelFactory(
                    groupRepository,
                    userRepository
                )
            ).get(ViewGroupViewModel::class.java)

        groupId = requireArguments().let {
            ViewGroupFragmentArgs.fromBundle(
                it
            ).groupId
        }
        viewModel.groupRequested(groupId)

        return inflater.inflate(R.layout.fragment_view_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.groupName.observe(viewLifecycleOwner, Observer {
            group_name.text = it
        })
        viewModel.totalMembers.observe(viewLifecycleOwner, Observer {
            member_count.text = it.toString() + " members"
        })
    }
}