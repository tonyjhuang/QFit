package com.tonyjhuang.qfit.ui.creategroup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tonyjhuang.qfit.R
import kotlinx.android.synthetic.main.fragment_create_group.*

class CreateGroupFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        group_name.setText(arguments?.getString(CreateGroupActivity.ARG_GROUP_NAME) ?: "")

        save.setOnClickListener {

        }
    }
}
