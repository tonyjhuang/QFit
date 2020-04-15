package com.tonyjhuang.qfit.ui.creategroup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tonyjhuang.qfit.SingleLiveEvent
import com.tonyjhuang.qfit.data.CurrentUserRepository
import com.tonyjhuang.qfit.data.GoalRepository
import com.tonyjhuang.qfit.data.GroupRepository

class CreateGroupViewModel(
    private val groupRepository: GroupRepository,
    private val currentUserRepository: CurrentUserRepository,
    private val goalRepository: GoalRepository
) : ViewModel() {

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _events = SingleLiveEvent<Event>()
    val events: LiveData<Event> = _events

    private val _goals = MutableLiveData<List<CreateGoal>>()
    val goals: LiveData<List<CreateGoal>> = _goals

    init {
        goalRepository.getAll { _goals.value = it?.map { CreateGoal(it.key, it.value.name!!) } }
    }

    fun createGroup(name: String) {
        groupRepository.getByName(name) { id, _ ->
            if (id != null) {
                _errorMessage.value = "Group already exists"
                return@getByName
            }
            createNewGroup(name)
        }
    }

    private fun createNewGroup(name: String) {
        currentUserRepository.getCurrentUser { uid, _ ->
            groupRepository.create(name, uid, emptyMap()) { gid, _ ->
                _events.value = Event.FinishEvent(gid)
            }
        }
    }

    sealed class Event {
        class FinishEvent(val id: String) : Event()
    }
}


data class CreateGoal(val id: String, val name: String)

class CreateGroupViewModelFactory(
    private val groupRepository: GroupRepository,
    private val currentUserRepository: CurrentUserRepository,
    private val goalRepository: GoalRepository
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(
            GroupRepository::class.java,
            CurrentUserRepository::class.java,
            GoalRepository::class.java
        ).newInstance(groupRepository, currentUserRepository, goalRepository)
    }
}