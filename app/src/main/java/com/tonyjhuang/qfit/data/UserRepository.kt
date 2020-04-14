package com.tonyjhuang.qfit.data

import com.tonyjhuang.qfit.data.models.User

class UserRepository(private val db: QfDb) {

    fun exists(id: String, callback: (Boolean) -> Unit) {
        db.exists(PATH, id, callback)
    }

    fun create(id: String, name: String, photoUrl: String, callback: (User) -> Unit) {
        db.create("users", id, User(name, photoUrl), callback)
    }

    companion object {
        const val PATH = "users"
    }
}