package com.tonyjhuang.qfit.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.GenericTypeIndicator
import com.tonyjhuang.qfit.SimpleValueEventListener
import com.tonyjhuang.qfit.SingleLiveEvent
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
    private val progressRepository: ProgressRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _header = MutableLiveData<String>()
    val header: LiveData<String> = _header
    private lateinit var currentUserId: String

    private val _userPhoto = MutableLiveData<String>()
    val userPhoto: LiveData<String> = _userPhoto

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

    private val _events = SingleLiveEvent<Event>()
    val events: LiveData<Event> = _events

    init {
        val df = SimpleDateFormat("MMMM dd", Locale.US)
        _header.value = "Today - ${df.format(today)}"
        setUpWatchers()
    }

    private fun setUpWatchers() {
        currentUserRepository.getCurrentUser { currentUserId, currentUser ->
            this.currentUserId = currentUserId
            _userPhoto.postValue(currentUser.photo_url!!)
            val userGroupIds = currentUser.groups?.keys?.toList() ?: emptyList()
            if (userGroupIds.isEmpty()) {
                _dailyUserProgress.postValue(emptyList())
                return@getCurrentUser
            }
            groupRepository.getByIds(userGroupIds) {
                handleUserGroups(it)
            }
            progressRepository.watchUserDailyProgress(currentUserId, today, userProgressListener)
        }
    }

    private fun handleUserGroups(userGroups: Map<String, Group>) {
        this.userGroups = userGroups
        val allGoalIds = userGroups.values.flatMap { it.metadata?.goals?.keys ?: emptySet() }
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
            var totalGroups = 0
            var finishedGroups = 0
            for ((groupId, group) in userGroups) {
                val groupGoals = group.metadata?.goals ?: continue
                if (groupGoals.keys.contains(goalId)) {
                    val groupGoalAmount = groupGoals[goalId]!!.amount
                    totalGroups += 1
                    finishedGroups += if (progress >= groupGoalAmount) 1 else 0
                    targets.add(GroupTarget(groupId, group.metadata!!.name!!, groupGoalAmount))
                }
            }
            res.add(
                DailyUserProgress(
                    goalId,
                    goalName,
                    progress,
                    targets,
                    totalGroups == finishedGroups
                )
            )
        }
        _dailyUserProgress.postValue(res)
    }

    fun updateUserProgress(goalId: String, userProgressDelta: Int) {
        if (userProgressDelta == 0) return
        val currentProgressAmount = userProgress[goalId]?.amount ?: 0
        val newProgressAmount = currentProgressAmount + userProgressDelta
        if (didAchieveNewGoal(goalId, currentProgressAmount, newProgressAmount)) {
            _events.postValue(Event.AchievedNewGoalEvent())
        }

        progressRepository.addUserProgress(
            currentUserId,
            goalId,
            today,
            newProgressAmount
        )

    }

    private fun didAchieveNewGoal(goalId: String, oldProgressAmount: Int, newProgressAmount: Int): Boolean {
        var achievedGoals = 0
        var newlyAchievedGoals = 0
        for (group in userGroups.values) {
            val groupGoalAmount = group.metadata?.goals?.get(goalId)?.amount ?: continue
            achievedGoals += if (oldProgressAmount >= groupGoalAmount) 1 else 0
            newlyAchievedGoals += if (newProgressAmount >= groupGoalAmount) 1 else 0
        }
        return achievedGoals < newlyAchievedGoals
    }

    override fun onCleared() {
        progressRepository.unwatchUserDailyProgress(currentUserId, today, userProgressListener)
        super.onCleared()
    }


    sealed class Event {
        class AchievedNewGoalEvent : Event()
    }
}

data class DailyUserProgress(
    val goalId: String,
    val goalName: String,
    val userProgress: Int,
    val groupTargets: List<GroupTarget>,
    val finished: Boolean = true
)


data class GroupTarget(val id: String, val groupName: String, val goalAmount: Int)


class HomeViewModelFactory(
    private val groupRepository: GroupRepository,
    private val currentUserRepository: CurrentUserRepository,
    private val goalRepository: GoalRepository,
    private val progressRepository: ProgressRepository,
    private val userRepository: UserRepository

) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(
            GroupRepository::class.java,
            CurrentUserRepository::class.java,
            GoalRepository::class.java,
            ProgressRepository::class.java,
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