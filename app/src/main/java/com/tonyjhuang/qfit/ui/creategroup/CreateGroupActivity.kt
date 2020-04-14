package com.tonyjhuang.qfit.ui.creategroup

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.tonyjhuang.qfit.R
import kotlinx.android.synthetic.main.activity_create_group.*


class CreateGroupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)

        val navHostFragment = nav_host_fragment as NavHostFragment

        val inflater = navHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.nav_create_group)
        navHostFragment.navController.setGraph(graph, Bundle().apply {
            putString(ARG_GROUP_NAME, intent.getStringExtra(ARG_GROUP_NAME))
        })
    }

    companion object {
        const val ARG_GROUP_NAME = "group_name"
    }
}
