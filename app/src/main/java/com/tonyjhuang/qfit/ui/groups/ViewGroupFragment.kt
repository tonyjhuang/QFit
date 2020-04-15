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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.tonyjhuang.qfit.R
import com.tonyjhuang.qfit.data.GroupRepository
import com.tonyjhuang.qfit.data.UserRepository
import com.tonyjhuang.qfit.ui.creategroup.CreateGroupActivity

class ViewGroupFragment : Fragment() {

    private lateinit var viewModel: ViewGroupViewModel
    private val adapter = GroupRecyclerViewAdapter(::onGroupItemClicked)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val userRepository = UserRepository(Firebase.database.reference)
        val groupRepository = GroupRepository(Firebase.database.reference, userRepository)
        viewModel =
            ViewModelProviders.of(
                this,
                ViewGroupViewModelFactory(groupRepository)
            ).get(ViewGroupViewModel::class.java)

        val view = inflater.inflate(R.layout.fragment_view_group, container, false)
        with(view.findViewById<RecyclerView>(R.id.list)) {
            layoutManager = LinearLayoutManager(context)
            adapter = this@ViewGroupFragment.adapter
        }
        view.findViewById<View>(R.id.add_group).setOnClickListener {
            getNewGroupName(viewModel::addNewGroup)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.text.observe(this, Observer {
            view.findViewById<TextView>(R.id.text_title).text = it
        })
        viewModel.groupList.observe(this, Observer {
            adapter.values = it
        })
        viewModel.events.observe(this, Observer {
            when(it) {
                is ViewGroupViewModel.Event.CreateNewGroupEvent -> {
                    launchCreateGroupFlow(it.name)
                }
                is ViewGroupViewModel.Event.ViewGroupEvent -> {
                    launchViewGroupFlow(it.name)
                }
            }
        })
    }

    private fun onGroupItemClicked(groupItem: GroupItem) {
        launchViewGroupFlow(groupItem.name)
    }

    private fun getNewGroupName(callback: (String) -> Unit) {
        val dialogBuilder = AlertDialog.Builder(context!!)
        val view: View? = LayoutInflater.from(context)?.inflate(R.layout.dialog_add_new_group, null)
        dialogBuilder
            .setView(view)
            .setCancelable(false)
            .setPositiveButton("Proceed", DialogInterface.OnClickListener { _, _ ->
                callback(view?.findViewById<EditText>(R.id.group_name)?.text.toString() )
            })
            .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, _ ->
                dialog.cancel()
            })
            .create()
            .show()
    }

    private fun launchViewGroupFlow(groupName: String) {

    }

    private fun launchCreateGroupFlow(newGroupName: String) {
        startActivityForResult(Intent(context, CreateGroupActivity::class.java).apply {
            putExtra(CreateGroupActivity.ARG_GROUP_NAME, newGroupName)
        }, RC_CREATE_GROUP)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_CREATE_GROUP) {
            if (resultCode == Activity.RESULT_OK) {
                //findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
            } else {
                // TODO Handle error
            }
        }
    }


    companion object {
        const val RC_CREATE_GROUP = 0
    }
}