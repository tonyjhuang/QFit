package com.tonyjhuang.qfit.ui.groups

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GroupListViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is group list Fragment"
    }
    val text: LiveData<String> = _text

    private val _groupList = MutableLiveData<List<GroupItem>>().apply {
        value = listOf(
            GroupItem("jazzercise", 10),
            GroupItem("runner's club", 25)
        )
    }
    val groupList: LiveData<List<GroupItem>> = _groupList
}