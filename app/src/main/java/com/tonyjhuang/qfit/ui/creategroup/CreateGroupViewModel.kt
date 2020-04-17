package com.tonyjhuang.qfit.ui.creategroup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tonyjhuang.qfit.SingleLiveEvent
import com.tonyjhuang.qfit.data.CurrentUserRepository
import com.tonyjhuang.qfit.data.GoalRepository
import com.tonyjhuang.qfit.data.GroupRepository
import com.tonyjhuang.qfit.data.models.Goal
import com.tonyjhuang.qfit.data.models.GroupGoal

class CreateGroupViewModel(
    private val groupRepository: GroupRepository,
    private val currentUserRepository: CurrentUserRepository,
    private val goalRepository: GoalRepository
) : ViewModel() {

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _events = SingleLiveEvent<Event>()
    val events: LiveData<Event> = _events

    private lateinit var goalRegistry: Map<String, Goal>
    private val _goals = MutableLiveData<List<CreateGoal>>()
    val goals: LiveData<List<CreateGoal>> = _goals

    init {
        goalRepository.getAll {
            goalRegistry = it
            _goals.value = it.map { CreateGoal(it.key, it.value.name!!) }
        }
    }

    fun createGroup(name: String, goals: Map<String, String>) {
        val sanitizedGoals = sanitizeGoalInput(goals)
        if (sanitizedGoals.isEmpty()) {
            _errorMessage.value = "At least one goal must be specified"
            return
        }
        groupRepository.getByName(name) { id, _ ->
            if (id != null) {
                _errorMessage.value = "Group already exists"
                return@getByName
            }
            createNewGroup(name, sanitizedGoals)
        }
    }

    fun sanitizeGoalInput(goals: Map<String, String>) = goals
        .mapValues { it.value.toIntOrNull() }
        .filter { goalRegistry.containsKey(it.key) && it.value != null && it.value != 0 }
        .mapValues { GroupGoal(it.value!!) }

    private fun createNewGroup(name: String, goals: Map<String, GroupGoal>) {
        currentUserRepository.getCurrentUser { uid, _ ->
            groupRepository.create(name, uid, goals) { gid, _ ->
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