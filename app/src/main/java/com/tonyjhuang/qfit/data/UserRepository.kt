package com.tonyjhuang.qfit.data

import com.tonyjhuang.qfit.data.models.User

class UserRepository(private val db: QfDb) {
    fun get(id: String, callback: (User?) -> Unit) {
        db.get(PATH, id, callback)
    }

    fun create(id: String, name: String, photoUrl: String, callback: (User?) -> Unit) {
        db.create(PATH, id, User(name, photoUrl)) { id, user ->
            callback(user)
        }
    }

    fun addGroup(id: String, groupId: String, callback: (User?) -> Unit) {
        db.create(PATH + "/groups", id) { id, _ ->
            get(id, callback)
        }
    }

    companion object {
        const val PATH = "users"
    }
}