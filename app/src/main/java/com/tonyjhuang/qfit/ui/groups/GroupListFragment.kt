package com.tonyjhuang.qfit.ui.groups

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.tonyjhuang.qfit.R
import com.tonyjhuang.qfit.data.CurrentUserRepository
import com.tonyjhuang.qfit.data.GroupRepository
import com.tonyjhuang.qfit.data.UserRepository
import com.tonyjhuang.qfit.ui.creategroup.CreateGroupActivity
import com.tonyjhuang.qfit.ui.creategroup.CreateGroupActivity.Companion.RES_GROUP_ID


class GroupListFragment : Fragment() {

    private lateinit var viewModel: GroupListViewModel
    private val adapter = GroupRecyclerViewAdapter(::onGroupItemClicked)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val userRepository = UserRepository(Firebase.database.reference)
        val currentUserRepository = CurrentUserRepository(userRepository)
        val groupRepository = GroupRepository(Firebase.database.reference, userRepository)
        viewModel =
            ViewModelProviders.of(
                this,
                GroupListViewModelFactory(currentUserRepository, userRepository, groupRepository)
            ).get(GroupListViewModel::class.java)

        val view = inflater.inflate(R.layout.fragment_group_list, container, false)
        view.findViewById<View>(R.id.add_group).setOnClickListener {
            getNewGroupName(viewModel::addNewGroup)
        }
        with(view.findViewById<RecyclerView>(R.id.list)) {
            layoutManager = LinearLayoutManager(context)
            adapter = this@GroupListFragment.adapter
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.text.observe(viewLifecycleOwner, Observer {
            view.findViewById<TextView>(R.id.text_title).text = it
        })
        viewModel.groupList.observe(viewLifecycleOwner, Observer {
            adapter.values = it
        })
        viewModel.events.observe(viewLifecycleOwner, Observer {
            when (it) {
                is GroupListViewModel.Event.CreateNewGroupEvent -> {
                    launchCreateGroupFlow(it.name)
                }
                is GroupListViewModel.Event.ViewGroupEvent -> {
                    launchViewGroupFlow(it.id)
                }
            }
        })
    }

    private fun onGroupItemClicked(groupItem: GroupItem) {
        launchViewGroupFlow(groupItem.id)
    }

    private fun getNewGroupName(callback: (String) -> Unit) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        val view: View? = LayoutInflater.from(context)?.inflate(R.layout.dialog_add_new_group, null)
        dialogBuilder
            .setView(view)
            .setCancelable(false)
            .setPositiveButton("Join", DialogInterface.OnClickListener { _, _ ->
                callback(view?.findViewById<EditText>(R.id.group_name)?.text.toString())
            })
            .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, _ ->
                dialog.cancel()
            })
            .create()
            .show()
    }

    private fun launchViewGroupFlow(groupId: String) {
        findNavController().navigate(GroupListFragmentDirections.actionGroupListToViewGroup(groupId))
    }

    private fun launchCreateGroupFlow(newGroupName: String) {
        startActivityForResult(Intent(context, CreateGroupActivity::class.java).apply {
            putExtra(CreateGroupActivity.ARG_GROUP_NAME, newGroupName)
        }, RC_CREATE_GROUP)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_CREATE_GROUP) {
            val groupId = data?.getStringExtra(RES_GROUP_ID)
            if (resultCode == Activity.RESULT_OK && groupId != null) {
                launchViewGroupFlow(groupId)
            } else {
                // TODO Handle error
            }
        }
    }


    companion object {
        const val RC_CREATE_GROUP = 0
    }
}

data class GroupItem(val id: String, val name: String, val totalMembers: Int)