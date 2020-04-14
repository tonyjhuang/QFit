package com.tonyjhuang.qfit.data

import com.tonyjhuang.qfit.data.models.Group
import com.tonyjhuang.qfit.data.models.GroupGoal
import com.tonyjhuang.qfit.data.models.User

class GroupRepository(private val db: QfDb) {
    fun exists(name: String, callback: (Boolean) -> Unit) {
        db.exists(PATH, name, callback)
    }

    fun create(name: String, goals: Map<String, GroupGoal>, callback: (Group) -> Unit) {
        db.create(PATH, name, Group(goals=goals), callback)
    }

    companion object {
        const val PATH = "groups"
    }
}