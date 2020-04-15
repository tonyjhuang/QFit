package com.tonyjhuang.qfit.ui.groups

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tonyjhuang.qfit.SingleLiveEvent
import com.tonyjhuang.qfit.data.GroupRepository

class ViewGroupViewModel(private val groupRepository: GroupRepository) : ViewModel() {

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

    private val _events = SingleLiveEvent<Event>()
    val events: LiveData<Event> = _events

    fun addNewGroup(name: String) {
        groupRepository.nameExists(name) {
            if (it) {
                _events.value = Event.ViewGroupEvent(name)
                return@nameExists
            }
            _events.value = Event.CreateNewGroupEvent(name)
        }
    }

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