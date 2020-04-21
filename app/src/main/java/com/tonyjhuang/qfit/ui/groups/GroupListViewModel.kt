package com.tonyjhuang.qfit.ui.groups

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.tonyjhuang.qfit.SimpleValueEventListener
import com.tonyjhuang.qfit.SingleLiveEvent
import com.tonyjhuang.qfit.data.CurrentUserRepository
import com.tonyjhuang.qfit.data.GroupRepository
import com.tonyjhuang.qfit.data.UserRepository
import com.tonyjhuang.qfit.data.models.Group

class GroupListViewModel(
    private val currentUserRepository: CurrentUserRepository,
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Your Groups"
    }
    val text: LiveData<String> = _text

    private val _groupList = MutableLiveData<List<GroupItem>>()
    val groupList: LiveData<List<GroupItem>> = _groupList

    private val _events = SingleLiveEvent<Event>()
    val events: LiveData<Event> = _events

    private lateinit var currentUserId: String
    private val userGroupListListener = object : SimpleValueEventListener() {
        override fun onDataChange(p0: DataSnapshot) {
            val groupIds =
                p0.getValue(object :
                    GenericTypeIndicator<Map<String, Boolean>>() {})?.keys?.toList() ?: emptyList()
            watchGroups(groupIds)
        }
    }

    private val groupListeners: MutableMap<String, ValueEventListener> = mutableMapOf()
    private val groupData: MutableMap<String, Group> = mutableMapOf()

    init {
        currentUserRepository.getCurrentUser { id, user ->
            currentUserId = id
            userRepository.watchUserGroups(id, userGroupListListener)
        }
    }

    fun addNewGroup(name: String) {
        // TODO join group if not already in
        groupRepository.getByName(name) { groupId, group ->
            if (groupId != null && group != null) {
                if (isUserInGroup(currentUserId, group)) {
                    _events.value = Event.ViewGroupEvent(groupId)
                } else {
                    groupRepository.addMember(groupId, currentUserId) {
                        _events.value = Event.ViewGroupEvent(groupId)
                    }
                }
                return@getByName
            }
            _events.value = Event.CreateNewGroupEvent(name)
        }
    }

    fun isUserInGroup(userId: String, group: Group): Boolean {
        return group.members?.get(userId) == true
    }

    fun watchGroups(groupIds: List<String>) {
        if (groupIds.isEmpty()) {
            _groupList.postValue(emptyList())
            return
        }
        for ((id, listener) in groupListeners) {
            groupRepository.unwatchGroup(id, listener)
        }
        groupListeners.clear()
        for (id in groupIds) {
            val listener = SimpleGroupListener(id)
            groupListeners[id] = listener
            groupRepository.watchGroup(id, listener)
        }
    }

    private fun notifyDataSetChanged() {
        val newGroupList = mutableListOf<GroupItem>()
        for ((id, group) in groupData) {
            newGroupList.add(GroupItem(id, group.metadata?.name ?: "", group.members?.size ?: 0))
        }
        _groupList.postValue(newGroupList)
    }

    override fun onCleared() {
        userRepository.unwatchUserGroups(currentUserId, userGroupListListener)
        for ((groupId, groupListener) in groupListeners) {
            groupRepository.unwatchGroup(groupId, groupListener)
        }
        super.onCleared()
    }


    inner class SimpleGroupListener(private val id: String) : SimpleValueEventListener() {
        override fun onDataChange(p0: DataSnapshot) {
            val group = p0.getValue(Group::class.java)
            if (group == null) {
                groupData.remove(id)
                return
            }
            groupData[id] = group
            notifyDataSetChanged()
        }
    }

    sealed class Event {
        class CreateNewGroupEvent(val name: String) : Event()
        class ViewGroupEvent(val id: String) : Event()
    }
}


class GroupListViewModelFactory(
    private val currentUserRepository: CurrentUserRepository,
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(
            CurrentUserRepository::class.java,
            UserRepository::class.java,
            GroupRepository::class.java
        ).newInstance(
            currentUserRepository,
            userRepository,
            groupRepository
        )
    }
}
