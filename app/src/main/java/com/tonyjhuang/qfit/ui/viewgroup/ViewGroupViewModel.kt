package com.tonyjhuang.qfit.ui.viewgroup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import com.tonyjhuang.qfit.SimpleValueEventListener
import com.tonyjhuang.qfit.data.GroupRepository
import com.tonyjhuang.qfit.data.UserRepository
import com.tonyjhuang.qfit.data.models.Group
import com.tonyjhuang.qfit.data.models.User

class ViewGroupViewModel(
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    //private val _events = SingleLiveEvent<Event>()
    //val events: LiveData<Event> = _events

    private val _groupName = MutableLiveData<String>()
    val groupName: LiveData<String> = _groupName

    private val _totalMembers = MutableLiveData(0)
    val totalMembers: LiveData<Int> = _totalMembers

    private val _groupGoals = MutableLiveData<Map<String, GroupGoalState>>()
    val groupGoals: LiveData<Map<String, GroupGoalState>> = _groupGoals

    private val userListeners: MutableMap<String, ValueEventListener> = mutableMapOf()
    private val userData: MutableMap<String, User> = mutableMapOf()

    private lateinit var groupId: String
    private val groupListener = object : SimpleValueEventListener() {
        override fun onDataChange(p0: DataSnapshot) {
            if (p0.exists()) {
                val group = p0.getValue(Group::class.java)
                group ?: return
                handleNewGroup(group)
            }
        }
    }

    fun groupRequested(id: String) {
        groupId = id
        groupRepository.watchGroup(id, groupListener)
    }

    fun watchUsers(userIds: List<String>) {
        for ((id, listener) in userListeners) {
            userRepository.unwatchUser(id, listener)
        }
        userListeners.clear()
        for (id in userIds) {
            val listener = SimpleUserListener(id)
            userListeners[id] = listener
            userRepository.watchUser(id, listener)
        }
        _totalMembers.postValue(userIds.size)
    }

    private fun handleNewGroup(group: Group) {
        _groupName.value = group.name
        watchUsers(group.members?.keys?.toList() ?: emptyList())
        _groupGoals.postValue(group.goals?.mapValues { (key, value) ->
            GroupGoalState(key, value.name!!, value.amount)
        })
    }


    private fun notifyDataSetChanged() {
        // TODO update member photo recyclerview
        /*val newGroupList = mutableListOf<GroupItem>()
        for ((id, group) in groupData) {
            newGroupList.add(GroupItem(id, group.name ?: "", group.members?.size ?: 0))
        }
        _groupList.postValue(newGroupList)*/
    }

    override fun onCleared() {
        groupRepository.unwatchGroup(groupId, groupListener)

        for ((userId, userListener) in userListeners) {
            userRepository.unwatchUserGroups(userId, userListener)
        }
        super.onCleared()
    }


    inner class SimpleUserListener(private val id: String) : SimpleValueEventListener() {
        override fun onDataChange(p0: DataSnapshot) {
            val user = p0.getValue(User::class.java)
            if (user == null) {
                userData.remove(id)
                return
            }
            userData[id] = user
            notifyDataSetChanged()
        }
    }
}


data class GroupGoalState(val id: String, val name: String, val amount: Int)


class ViewGroupViewModelFactory(
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(GroupRepository::class.java, UserRepository::class.java)
            .newInstance(groupRepository, userRepository)
    }
}