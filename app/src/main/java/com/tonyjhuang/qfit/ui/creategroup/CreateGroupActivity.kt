package com.tonyjhuang.qfit.ui.creategroup

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tonyjhuang.qfit.R
import kotlinx.android.synthetic.main.activity_create_group.*


class CreateGroupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)
    }

    companion object {
        const val ARG_GROUP_NAME = "group_name"
    }
}
