package com.tonyjhuang.qfit.ui.groups

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tonyjhuang.qfit.R
import kotlinx.android.synthetic.main.dialog_add_new_group.view.*

class GroupListFragment : Fragment() {

    private lateinit var groupListViewModel: GroupListViewModel
    private val adapter = GroupRecyclerViewAdapter(::onGroupItemClicked)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        groupListViewModel =
            ViewModelProviders.of(this).get(GroupListViewModel::class.java)

        val view = inflater.inflate(R.layout.fragment_group_list, container, false)
        with(view.findViewById<RecyclerView>(R.id.list)) {
            layoutManager = LinearLayoutManager(context)
            adapter = this@GroupListFragment.adapter
        }
        view.findViewById<View>(R.id.add_group).setOnClickListener {
            getNewGroupName(groupListViewModel::addNewGroup)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        groupListViewModel.text.observe(this, Observer {
            view.findViewById<TextView>(R.id.text_title).text = it
        })
        groupListViewModel.groupList.observe(this, Observer {
            adapter.values = it
        })
    }

    private fun onGroupItemClicked(groupItem: GroupItem) {

    }

    private fun getNewGroupName(callback: (String) -> Unit) {
        val dialogBuilder = AlertDialog.Builder(context!!)
        val view = LayoutInflater.from(context)?.inflate(R.layout.dialog_add_new_group, null)
        dialogBuilder
            .setView(view)
            .setCancelable(false)
            .setPositiveButton("Proceed", DialogInterface.OnClickListener { _, _ ->
                callback(view?.group_name?.text.toString() ?: "")
            })
            .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, _ ->
                dialog.cancel()
            })
            .create()
            .show()
    }
}

data class GroupItem(val name: String, val totalMembers: Int)