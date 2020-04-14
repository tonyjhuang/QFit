package com.tonyjhuang.qfit.ui.groups

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tonyjhuang.qfit.R

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

        groupListViewModel.text.observe(this, Observer {
            view.findViewById<TextView>(R.id.text_title).text = it
        })
        groupListViewModel.groupList.observe(this, Observer {
            adapter.values = it
        })

        return view
    }

    private fun onGroupItemClicked(groupItem: GroupItem) {

    }
}

data class GroupItem(val name: String, val totalMembers: Int)