package com.tonyjhuang.qfit.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.GenericTypeIndicator
import com.tonyjhuang.qfit.SimpleValueEventListener
import com.tonyjhuang.qfit.data.*
import com.tonyjhuang.qfit.data.models.Goal
import com.tonyjhuang.qfit.data.models.Group
import com.tonyjhuang.qfit.data.models.UserProgress
import java.text.SimpleDateFormat
import java.util.*


class HomeViewModel(
    private val groupRepository: GroupRepository,
    private val currentUserRepository: CurrentUserRepository,
    private val goalRepository: GoalRepository,
    private val progressRepository: UserProgressRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _header = MutableLiveData<String>()
    val header: LiveData<String> = _header
    private lateinit var currentUserId: String

    private lateinit var userGroups: Map<String, Group>
    private lateinit var userGoals: Map<String, Goal>
    private lateinit var userProgress: Map<String, UserProgress>

    private val today = Calendar.getInstance().time

    private val userProgressListener = object : SimpleValueEventListener() {
        override fun onDataChange(p0: DataSnapshot) {
            if (p0.exists()) {
                handleUserProgress(p0.getValue(object :
                    GenericTypeIndicator<Map<String, UserProgress>>() {})!!)
                return
            }
            handleUserProgress(emptyMap())
        }
    }

    private val _dailyUserProgress = MutableLiveData<List<DailyUserProgress>>()
    val dailyUserProgress: LiveData<List<DailyUserProgress>> = _dailyUserProgress


    init {
        val df = SimpleDateFormat("MMMM dd", Locale.US)
        _header.value = "Today - ${df.format(today)}"
        setUpWatchers()
    }

    private fun setUpWatchers() {
        currentUserRepository.getCurrentUser { currentUserId, currentUser ->
            this.currentUserId = currentUserId
            val userGroups = currentUser.groups ?: return@getCurrentUser
            groupRepository.getByIds(userGroups.keys.toList()) {
                handleUserGroups(it.filterValues { it != null } as Map<String, Group>)
            }
            progressRepository.watchDailyProgress(currentUserId, today, userProgressListener)
        }
    }

    private fun handleUserGroups(userGroups: Map<String, Group>) {
        this.userGroups = userGroups
        val allGoalIds = userGroups.values.flatMap { it.goals?.keys ?: emptySet() }
        goalRepository.getByIds(allGoalIds) {
            this.userGoals = it
            emitChanges()
        }
    }

    private fun handleUserProgress(userProgress: Map<String, UserProgress>) {
        this.userProgress = userProgress
        emitChanges()
    }

    private fun isDataReady() =
        ::userGoals.isInitialized && ::userGroups.isInitialized && ::userProgress.isInitialized

    private fun emitChanges() {
        if (!isDataReady()) return
        val res = mutableListOf<DailyUserProgress>()
        for (goalId in userGoals.keys) {
            val goalName = userGoals[goalId]!!.name!!
            val progress = userProgress[goalId]?.amount ?: 0
            val targets = mutableListOf<GroupTarget>()
            for ((groupId, group) in userGroups) {
                val groupGoals = group.goals ?: continue
                if (groupGoals.keys.contains(goalId)) {
                    targets.add(GroupTarget(groupId, group.name!!, groupGoals[goalId]!!.amount))
                }
            }
            res.add(DailyUserProgress(goalId, goalName, progress, targets))
        }
        _dailyUserProgress.postValue(res)
    }

    fun updateUserProgress(goalId: String, newUserProgressAmount: Int) {
        progressRepository.addProgress(
            currentUserId,
            goalId,
            today,
            (userProgress[goalId]?.amount ?: 0) + newUserProgressAmount
        )

    }

    override fun onCleared() {
        progressRepository.unwatchDailyProgress(currentUserId, today, userProgressListener)
        super.onCleared()
    }
}

data class DailyUserProgress(
    val goalId: String,
    val goalName: String,
    val userProgress: Int,
    val groupTargets: List<GroupTarget>
)


data class GroupTarget(val id: String, val groupName: String, val goalAmount: Int)


class HomeViewModelFactory(
    private val groupRepository: GroupRepository,
    private val currentUserRepository: CurrentUserRepository,
    private val goalRepository: GoalRepository,
    private val progressRepository: UserProgressRepository,
    private val userRepository: UserRepository

) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(
            GroupRepository::class.java,
            CurrentUserRepository::class.java,
            GoalRepository::class.java,
            UserProgressRepository::class.java,
            UserRepository::class.java
        ).newInstance(
            groupRepository,
            currentUserRepository,
            goalRepository,
            progressRepository,
            userRepository
        )
    }
}