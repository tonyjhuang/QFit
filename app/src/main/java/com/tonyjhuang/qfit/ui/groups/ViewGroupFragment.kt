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

    //private lateinit var viewModel: ViewGroupViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /*val userRepository = UserRepository(Firebase.database.reference)
        val groupRepository = GroupRepository(Firebase.database.reference, userRepository)
        viewModel =
            ViewModelProviders.of(
                this,
                ViewGroupViewModelFactory(groupRepository)
            ).get(ViewGroupViewModel::class.java)
*/
        val view = inflater.inflate(R.layout.fragment_view_group, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}