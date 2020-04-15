package com.tonyjhuang.qfit.ui.groups

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tonyjhuang.qfit.SingleLiveEvent
import com.tonyjhuang.qfit.data.GroupRepository

class ViewGroupViewModel(private val groupRepository: GroupRepository) : ViewModel() {


    private val _events = SingleLiveEvent<Event>()
    val events: LiveData<Event> = _events

    sealed class Event {
        class CreateNewGroupEvent(val name: String) : Event()
        class ViewGroupEvent(val name: String) : Event()
    }
}


class ViewGroupViewModelFactory(private val groupRepository: GroupRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(GroupRepository::class.java)
            .newInstance(groupRepository)
    }
}