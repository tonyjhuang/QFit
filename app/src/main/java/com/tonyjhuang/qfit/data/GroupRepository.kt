package com.tonyjhuang.qfit.data

import com.tonyjhuang.qfit.data.models.Group
import com.tonyjhuang.qfit.data.models.GroupGoal

class GroupRepository(private val db: QfDb,
                      private val userRepository: UserRepository) {
    fun nameExists(name: String, callback: (Boolean) -> Unit) {
        db.stringValueExists(PATH, "name", name, callback)
    }

    fun create(
        name: String,
        creatorId: String,
        goals: Map<String, GroupGoal>,
        callback: (String, Group) -> Unit
    ) {
        db.create(
            PATH,
            Group(name = name, members = mapOf(creatorId to true), goals = goals)
        ) { groupId, group ->
            userRepository.addGroup(creatorId, groupId) {
                callback(groupId, group)
            }
        }
    }

    companion object {
        const val PATH = "groups"
    }
}