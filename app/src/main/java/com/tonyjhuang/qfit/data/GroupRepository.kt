package com.tonyjhuang.qfit.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.tonyjhuang.qfit.data.models.Group
import com.tonyjhuang.qfit.data.models.GroupGoal

class GroupRepository(
    private val db: DatabaseReference,
    private val userRepository: UserRepository
) {
    fun getByName(name: String, callback: (Group?) -> Unit) {
        db.child(PATH)
            .orderByChild("name")
            .equalTo(name)
            .addListenerForSingleValueEvent(GroupValueListener(callback))
    }

    fun create(
        name: String,
        creatorId: String,
        goals: Map<String, GroupGoal>,
        callback: (String, Group) -> Unit
    ) {
        val group = Group(name = name, members = mapOf(creatorId to true), goals = goals)
        val newRef = db.child(PATH).push()
        val groupId = newRef.key!!
        newRef.setValue(group)
        userRepository.addGroupMembership(creatorId, groupId)
        callback(groupId, group)
    }

    class GroupValueListener(private val callback: (Group?) -> Unit) : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
            callback(null)
        }

        override fun onDataChange(p0: DataSnapshot) {
            if (p0.exists()) {
                callback(p0.getValue(Group::class.java))
                return
            }
            callback(null)
        }
    }

    companion object {
        const val PATH = "groups"
    }
}