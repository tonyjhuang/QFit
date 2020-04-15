package com.tonyjhuang.qfit.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.tonyjhuang.qfit.data.models.User

class UserRepository(private val db: DatabaseReference) {
    fun get(id: String, callback: (User?) -> Unit) {
        db.child(PATH)
            .child(id)
            .addListenerForSingleValueEvent(UserValueListener(callback))
    }

    fun create(id: String, name: String, photoUrl: String, callback: (User?) -> Unit) {
        val user = User(name, photoUrl)
        db.child(PATH)
            .child(id)
            .setValue(user)
            .addOnCompleteListener { callback(user) }
            .addOnFailureListener { callback(null) }
    }

    fun addGroupMembership(id: String, groupId: String) {
        db.child("$PATH/$id/groups/$groupId")
            .setValue(true)
    }

    fun removeGroupMembership(id: String, groupId: String) {
        db.child("$PATH/$id/groups/$groupId")
            .removeValue()
    }

    class UserValueListener(private val callback: (User?) -> Unit) : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
            callback(null)
        }

        override fun onDataChange(p0: DataSnapshot) {
            if (p0.exists()) {
                callback(p0.getValue(User::class.java))
                return
            }
            callback(null)
        }
    }

    companion object {
        const val PATH = "users"
    }
}