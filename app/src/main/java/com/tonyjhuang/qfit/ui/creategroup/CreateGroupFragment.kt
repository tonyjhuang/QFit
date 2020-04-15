package com.tonyjhuang.qfit.ui.creategroup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.tonyjhuang.qfit.R
import kotlinx.android.synthetic.main.fragment_create_group.*

class CreateGroupFragment : Fragment() {

    private val viewModel: CreateGroupViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        group_name.setText(arguments?.getString(CreateGroupActivity.ARG_GROUP_NAME) ?: "")
        viewModel.errorMessage.observe(viewLifecycleOwner, Observer {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        })
        save.setOnClickListener {
            viewModel.createGroup(group_name.text.toString())
        }
    }
}
