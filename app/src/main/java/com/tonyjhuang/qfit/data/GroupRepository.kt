package com.tonyjhuang.qfit.data

import com.google.firebase.database.*
import com.tonyjhuang.qfit.QLog
import com.tonyjhuang.qfit.data.models.Group
import com.tonyjhuang.qfit.data.models.GroupGoal
import com.tonyjhuang.qfit.data.models.GroupMetadata

class GroupRepository(
    private val db: DatabaseReference,
    private val userRepository: UserRepository
) {
    fun getByName(name: String, callback: (String?, Group?) -> Unit) {
        db.child(PATH)
            .orderByChild("metadata/name")
            .equalTo(name)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    callback(null, null)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (!p0.exists()) {
                        callback(null, null)
                        return
                    }
                    val value = p0.getValue(object :
                        GenericTypeIndicator<Map<String, Group>>() {})!!
                    val key: String = value.keys.first()
                    callback(key, value[key])
                }
            })
    }

    fun getById(id: String, callback: (Group?) -> Unit) {
        db.child("$PATH/$id")
            .addListenerForSingleValueEvent(GroupValueListener { _, group ->
                callback(group)
            })
    }

    fun watchGroup(id: String, listener: ValueEventListener) {
        db.child("$PATH/$id").addValueEventListener(listener)
    }

    fun unwatchGroup(id: String, listener: ValueEventListener) {
        db.child("$PATH/$id").removeEventListener(listener)
    }

    fun getByIds(ids: List<String>, callback: (Map<String, Group>) -> Unit) {
        val results: MutableMap<String, Group> = mutableMapOf()
        var remaining = ids.size

        for (id in ids) {
            getById(id) {
                if (it != null) results[id] = it
                remaining--
                if (remaining == 0) {
                    callback(results)
                }
            }
        }
    }

    fun addMember(groupId: String, userId: String, callback: () -> Unit) {
        db.child("$PATH/$groupId/members/$userId")
            .setValue(true)
            .addOnCompleteListener {
                userRepository.addGroupMembership(userId, groupId)
                callback()
            }
    }

    fun removeMember(groupId: String, userId: String, callback: () -> Unit) {
        db.child("$PATH/$groupId/members/$userId")
            .removeValue()
            .addOnCompleteListener {
                callback()
            }
    }

    fun delete(groupId: String, callback: () -> Unit) {
        db.child("$PATH/$groupId")
            .removeValue()
            .addOnCompleteListener {
                callback()
            }
    }

    fun create(
        name: String,
        creatorId: String,
        goals: Map<String, GroupGoal>,
        callback: (String, Group) -> Unit
    ) {
        val metadata = GroupMetadata(
            name = name,
            creatorId = creatorId,
            goals = goals
        )
        val group = Group(metadata = metadata, members = mapOf(creatorId to true))
        val newGroupRef = db.child(PATH).push()
        val groupId = newGroupRef.key!!
        newGroupRef.setValue(group)
            .addOnSuccessListener {
                userRepository.addGroupMembership(creatorId, groupId)
                callback(groupId, group)
            }
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