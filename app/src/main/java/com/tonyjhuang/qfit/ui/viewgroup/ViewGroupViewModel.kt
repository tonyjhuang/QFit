package com.tonyjhuang.qfit.ui.viewgroup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.GenericTypeIndicator
import com.tonyjhuang.qfit.QTime
import com.tonyjhuang.qfit.SimpleValueEventListener
import com.tonyjhuang.qfit.SingleLiveEvent
import com.tonyjhuang.qfit.data.*
import com.tonyjhuang.qfit.data.models.Goal
import com.tonyjhuang.qfit.data.models.Group
import com.tonyjhuang.qfit.data.models.User
import com.tonyjhuang.qfit.data.models.UserProgress
import java.util.*

class ViewGroupViewModel(
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository,
    private val goalRepository: GoalRepository,
    private val progressRepository: ProgressRepository,
    private val currentUserRepository: CurrentUserRepository
) : ViewModel() {

    private val _groupName = MutableLiveData<String>()
    val groupName: LiveData<String> = _groupName

    private val _groupMembers = MutableLiveData<List<GroupMember>>()
    val groupMembers: LiveData<List<GroupMember>> = _groupMembers

    private lateinit var groupData: Group
    private var userData: Map<String, User> = emptyMap()
    private var groupGoalData: Map<String, Goal> = emptyMap()

    private val _groupGoalProgress = MutableLiveData<Map<String, GroupGoalProgressView>>()
    val groupGoalProgress: LiveData<Map<String, GroupGoalProgressView>> = _groupGoalProgress

    private val groupDailyProgressListener = GroupDailyProgressListener()

    private val today = QTime.today
    private lateinit var groupId: String
    private val groupListener = object : SimpleValueEventListener() {
        override fun onDataChange(p0: DataSnapshot) {
            if (p0.exists()) {
                val group = p0.getValue(Group::class.java)
                group ?: return
                handleNewGroup(group)
            } else {
                _events.postValue(Event.GroupDisbandedEvent())
            }
        }
    }

    private lateinit var currentUserId: String

    private val _events = SingleLiveEvent<Event>()
    val events: LiveData<Event> = _events

    val isCurrentUserAdmin: Boolean
    get() = currentUserId == groupData.metadata?.creatorId

    fun groupRequested(groupId: String) {
        this.groupId = groupId
        currentUserRepository.getCurrentUser { currentUserId, _ ->
            this.currentUserId = currentUserId
            groupRepository.watchGroup(groupId, groupListener)
        }
    }

    private fun handleNewGroup(group: Group) {
        groupData = group
        _groupName.postValue(group.metadata!!.name!!)

        val memberIds = group.members?.keys?.toList() ?: emptyList()
        val groupGoals = group.metadata.goals ?: return
        goalRepository.getByIds(groupGoals.keys.toList()) {
            groupGoalData = it
            userRepository.getByIds(memberIds) {
                userData = it
                _groupMembers.postValue(it.map { (id, user) ->
                    GroupMember(id, user.photo_url!!)
                })
                watchGroupProgress(groupId)
            }
        }
    }

    private fun watchGroupProgress(groupId: String) {
        progressRepository.watchGroupDailyProress(groupId, today, groupDailyProgressListener)
    }

    override fun onCleared() {
        groupRepository.unwatchGroup(groupId, groupListener)
        progressRepository.unwatchGroupDailyProress(groupId, today, groupDailyProgressListener)
        super.onCleared()
    }

    inner class GroupDailyProgressListener : SimpleValueEventListener() {
        override fun onDataChange(p0: DataSnapshot) {
            if (!p0.exists()) {
                generateAndEmitGroupGoalProgressView(emptyMap())
                return
            }
            generateAndEmitGroupGoalProgressView(p0.getValue(object :
                GenericTypeIndicator<Map<String, Map<String, UserProgress>>>() {})!!)
        }
    }

    private fun generateAndEmitGroupGoalProgressView(serverGroupProgress: Map<String, Map<String, UserProgress>>) {
        val res = mutableMapOf<String, GroupGoalProgressView>()
        for ((goalId, goal) in groupGoalData) {
            val groupGoal = groupData.metadata?.goals?.get(goalId) ?: continue
            res[goalId] =
                GroupGoalProgressView(
                    goalId,
                    goal.name!!,
                    groupGoal.amount,
                    generateCurrentUserProgress(goalId, serverGroupProgress[goalId])
                )
        }
        _groupGoalProgress.postValue(res)
    }

    private fun generateCurrentUserProgress(
        goalId: String,
        serverUserProgress: Map<String, UserProgress>?
    ): List<CurrentUserProgress> {
        val res = mutableListOf<CurrentUserProgress>()
        val groupGoalTarget = groupData.metadata?.goals?.get(goalId)?.amount ?: 0
        for ((userId, user) in userData) {
            val userProgressAmount = serverUserProgress?.get(userId)?.amount ?: 0
            res.add(
                CurrentUserProgress(
                    userId,
                    user.name!!,
                    user.photo_url!!,
                    userProgressAmount,
                    currentUserId == userId,
                    userProgressAmount >= groupGoalTarget,
                    userProgressAmount > 0
                )
            )
        }
        return res.sortedByDescending { it.userProgressAmount }
    }

    fun updateUserProgress(goalId: String, userProgressDelta: Int) {
        if (userProgressDelta == 0) return
        val currentUserProgressAmount =
            _groupGoalProgress.value?.get(goalId)?.userProgress?.first {
                it.userId == currentUserId
            }?.userProgressAmount ?: return

        val newProgressAmount = currentUserProgressAmount + userProgressDelta
        if (didAchieveNewGoal(goalId, currentUserProgressAmount, newProgressAmount)) {
            _events.postValue(Event.AchievedNewGoalEvent())
        }

        progressRepository.addUserProgress(
            currentUserId,
            goalId,
            today,
            newProgressAmount
        )
    }

    private fun didAchieveNewGoal(
        goalId: String,
        oldProgressAmount: Int,
        newProgressAmount: Int
    ): Boolean {
        val groupGoalAmount = groupData.metadata?.goals?.get(goalId)?.amount ?: return false
        return groupGoalAmount in (oldProgressAmount + 1)..newProgressAmount
    }

    fun deleteGroup() {
        groupRepository.unwatchGroup(groupId, groupListener)
        groupRepository.delete(groupId) {
            _events.postValue(Event.GroupDeletedEvent())
        }
    }

    fun leaveGroup() {
        groupRepository.removeMember(groupId, currentUserId) {
            _events.postValue(Event.LeftGroupEvent())
        }
    }

    sealed class Event {
        class AchievedNewGoalEvent : Event()
        class GroupDeletedEvent: Event()
        class LeftGroupEvent: Event()
        class GroupDisbandedEvent: Event()
    }
}

data class GroupMember(
    val userId: String,
    val userPhotoUrl: String
)

data class GroupGoalProgressView(
    val goalId: String,
    val goalName: String,
    val goalAmount: Int,
    val userProgress: List<CurrentUserProgress>
)

data class CurrentUserProgress(
    val userId: String,
    val userName: String,
    val userPhotoUrl: String,
    val userProgressAmount: Int,
    val isCurrentUser: Boolean,
    val finished: Boolean,
    val inProgress: Boolean
)


class ViewGroupViewModelFactory(
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository,
    private val goalRepository: GoalRepository,
    private val progressRepository: ProgressRepository,
    private val currentUserRepository: CurrentUserRepository
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(
            GroupRepository::class.java,
            UserRepository::class.java,
            GoalRepository::class.java,
            ProgressRepository::class.java,
            CurrentUserRepository::class.java
        ).newInstance(
            groupRepository,
            userRepository,
            goalRepository,
            progressRepository,
            currentUserRepository
        )
    }
}