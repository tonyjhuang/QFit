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
    fun getByName(name: String, callback: (String?, Group?) -> Unit) {
        db.child(PATH)
            .orderByChild("name")
            .equalTo(name)
            .addListenerForSingleValueEvent(GroupValueListener(callback))
    }

    fun getById(id: String, callback: (Group?) -> Unit) {
        db.child(PATH)
            .child(id)
            .addListenerForSingleValueEvent(GroupValueListener { _, group ->
                callback(group)
            })
    }

    fun watchGroup(id: String, listener: ValueEventListener) {
        db.child(PATH).child(id).addValueEventListener(listener)
    }

    fun unwatchGroup(id: String, listener: ValueEventListener) {
        db.child(PATH).child(id).removeEventListener(listener)
    }

    fun getByIds(ids: List<String>, callback: (Map<String, Group?>) -> Void) {
        val results = ids.associateWith { null as Group? }.toMutableMap()
        var remaining = ids.size

        for (id in ids) {
            getById(id) {
                results[id] = it
                remaining--
                if (remaining == 0) {
                    callback(results)
                }
            }
        }
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

    class GroupValueListener(private val callback: (String?, Group?) -> Unit) : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
            callback(null, null)
        }

        override fun onDataChange(p0: DataSnapshot) {
            if (p0.exists()) {
                callback(p0.key, p0.getValue(Group::class.java))
                return
            }
            callback(null, null)
        }
    }

    companion object {
        const val PATH = "groups"
    }
}