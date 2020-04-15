package com.tonyjhuang.qfit.ui.creategroup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.tonyjhuang.qfit.R
import com.tonyjhuang.qfit.data.CurrentUserRepository
import com.tonyjhuang.qfit.data.GroupRepository
import com.tonyjhuang.qfit.data.UserRepository
import kotlinx.android.synthetic.main.activity_create_group.*


class CreateGroupActivity : AppCompatActivity() {

    private lateinit var viewModel: CreateGroupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)

        val userRepository = UserRepository(Firebase.database.reference)
        val currentUserRepository = CurrentUserRepository(userRepository)
        val groupRepository = GroupRepository(Firebase.database.reference, userRepository)
        viewModel =
            ViewModelProviders.of(
                this,
                CreateGroupViewModelFactory(groupRepository, currentUserRepository)
            ).get(CreateGroupViewModel::class.java)

        val navHostFragment = nav_host_fragment as NavHostFragment
        val inflater = navHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.nav_create_group)
        navHostFragment.navController.setGraph(graph, Bundle().apply {
            putString(ARG_GROUP_NAME, intent.getStringExtra(ARG_GROUP_NAME))
        })

        viewModel.events.observe(this, Observer {
            when (it) {
                is CreateGroupViewModel.Event.FinishEvent -> {
                    setResult(Activity.RESULT_OK, Intent().apply {
                        putExtra(RES_GROUP_ID, it.id)
                    })
                    finish()
                }
            }
        })
    }

    companion object {
        const val ARG_GROUP_NAME = "group_name"
        const val RES_GROUP_ID = "group_id"
    }
}
