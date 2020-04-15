package com.tonyjhuang.qfit.ui.creategroup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tonyjhuang.qfit.SingleLiveEvent
import com.tonyjhuang.qfit.data.CurrentUserRepository
import com.tonyjhuang.qfit.data.GroupRepository
import com.tonyjhuang.qfit.data.UserRepository

class CreateGroupViewModel(
    private val currentUserRepository: CurrentUserRepository,
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _events = SingleLiveEvent<Event>()
    val events: LiveData<Event> = _events

    fun createGroup(name: String) {
        groupRepository.getByName(name) {
            if (it != null) {
                _errorMessage.value = "Group already exists"
                return@getByName
            }
            createNewGroup(name)
        }
    }

    private fun createNewGroup(name: String) {
        currentUserRepository.getCurrentUser { uid, user ->
            if (uid == null || user == null) {
                _errorMessage.value = "Couldn't get current user"
                return@getCurrentUser
            }
            groupRepository.create(name, uid, emptyMap()) { gid, _ ->
                _events.value = Event.FinishEvent(gid)
            }
        }
    }

    sealed class Event {
        class FinishEvent(val id: String) : Event()
    }
}


class CreateGroupViewModelFactory(private val groupRepository: GroupRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(GroupRepository::class.java)
            .newInstance(groupRepository)
    }
}