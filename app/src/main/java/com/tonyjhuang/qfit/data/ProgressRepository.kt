package com.tonyjhuang.qfit.data

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.tonyjhuang.qfit.toIso
import java.util.*

class ProgressRepository(
    private val db: DatabaseReference,
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository
) {

    fun watchUserDailyProgress(
        userId: String,
        date: Date,
        listener: ValueEventListener
    ) {
        db.child("$USER_PATH/$userId/${date.toIso()}")
            .addValueEventListener(listener)
    }

    fun unwatchUserDailyProgress(
        userId: String,
        date: Date,
        listener: ValueEventListener
    ) {
        db.child("$USER_PATH/$userId/${date.toIso()}")
            .removeEventListener(listener)
    }

    fun addUserProgress(
        userId: String,
        goalId: String,
        date: Date,
        newProgressAmount: Int
    ) {
        db.child("$USER_PATH/$userId/${date.toIso()}/$goalId/amount").setValue(newProgressAmount)

        userRepository.getById(userId) { user ->
            user ?: return@getById
            groupRepository.getByIds(user.groups?.keys?.toList() ?: emptyList()) { groups ->
                if (groups.isEmpty()) return@getByIds
                val groupsToUpdate =
                    groups.filterValues { it.goals?.containsKey(goalId) == true }.keys
                for (groupId in groupsToUpdate) {
                    db.child("$GROUP_PATH/$groupId/${date.toIso()}/$goalId/$userId/amount")
                        .setValue(newProgressAmount)
                }
            }
        }
    }

    fun watchGroupDailyProress(groupId: String, date: Date, listener: ValueEventListener) {
        db.child("$GROUP_PATH/$groupId/${date.toIso()}").addValueEventListener(listener)
    }

    fun unwatchGroupDailyProress(groupId: String, date: Date, listener: ValueEventListener) {
        db.child("$GROUP_PATH/$groupId/${date.toIso()}").removeEventListener(listener)
    }

    companion object {
        const val USER_PATH = "user_progress"
        const val GROUP_PATH = "group_progress"
    }
}
