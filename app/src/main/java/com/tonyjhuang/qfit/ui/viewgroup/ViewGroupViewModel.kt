package com.tonyjhuang.qfit.ui.viewgroup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.DataSnapshot
import com.tonyjhuang.qfit.SimpleValueEventListener
import com.tonyjhuang.qfit.data.GoalRepository
import com.tonyjhuang.qfit.data.GroupRepository
import com.tonyjhuang.qfit.data.ProgressRepository
import com.tonyjhuang.qfit.data.UserRepository
import com.tonyjhuang.qfit.data.models.Goal
import com.tonyjhuang.qfit.data.models.Group
import com.tonyjhuang.qfit.data.models.User
import com.tonyjhuang.qfit.data.models.UserProgress
import java.util.*

class ViewGroupViewModel(
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository,
    private val goalRepository: GoalRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    private val _groupName = MutableLiveData<String>()
    val groupName: LiveData<String> = _groupName

    private val _totalMembers = MutableLiveData(0)
    val totalMembers: LiveData<Int> = _totalMembers

    private lateinit var groupData: Group
    private var userData: Map<String, User> = emptyMap()
    private var groupGoalData: Map<String, Goal> = emptyMap()

    private val _groupGoalProgress = MutableLiveData<Map<String, GroupGoalProgressView>>()
    val groupGoalProgress: LiveData<Map<String, GroupGoalProgressView>> = _groupGoalProgress

    private val groupDailyProgressListener = GroupDailyProgressListener()

    private val today = Calendar.getInstance().time
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


    private fun handleNewGroup(group: Group) {
        groupData = group
        _groupName.postValue(group.name)

        val memberIds = group.members?.keys?.toList() ?: emptyList()
        _totalMembers.postValue(memberIds.size)

        val groupGoals = group.goals ?: return
        goalRepository.getByIds(groupGoals.keys.toList()) {
            groupGoalData = it
            userRepository.getByIds(memberIds) {
                userData = it
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
        }
    }

    fun generateAndEmitGroupGoalProgressView(serverGroupProgress: Map<String, Map<String, UserProgress>>) {
        val res = mutableMapOf<String, GroupGoalProgressView>()
        for ((goalId, goal) in groupGoalData) {
            val groupGoal = groupData.goals?.get(goalId) ?: continue
            res[goalId] =
                GroupGoalProgressView(
                    goalId,
                    goal.name!!,
                    groupGoal.amount,
                    generateCurrentUserProgress(serverGroupProgress[goalId])
                )
        }
        _groupGoalProgress.postValue(res)
    }

    private fun generateCurrentUserProgress(
        serverUserProgress: Map<String, UserProgress>?
    ): List<CurrentUserProgress> {
        val res = mutableListOf<CurrentUserProgress>()
        for ((userId, user) in userData) {
            res.add(
                CurrentUserProgress(
                    userId,
                    user.photo_url!!,
                    serverUserProgress?.get(userId)?.amount ?: 0
                )
            )
        }
        return res.sortedByDescending { it.userProgressAmount }
    }
}

data class GroupGoalProgressView(
    val goalId: String,
    val goalName: String,
    val goalAmount: Int,
    val userProgress: List<CurrentUserProgress>
)

data class CurrentUserProgress(
    val userId: String,
    val userPhotoUrl: String,
    val userProgressAmount: Int
)


class ViewGroupViewModelFactory(
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository,
    private val goalRepository: GoalRepository,
    private val progressRepository: ProgressRepository
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(
            GroupRepository::class.java,
            UserRepository::class.java,
            GoalRepository::class.java,
            ProgressRepository::class.java
        ).newInstance(groupRepository, userRepository, goalRepository, progressRepository)
    }
}