package com.tonyjhuang.qfit.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.tonyjhuang.qfit.data.models.User
import java.lang.RuntimeException

class CurrentUserRepository(private val userRepository: UserRepository) {
    fun fetchCurrentUser(callback: (String?, User?) -> Unit) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        firebaseUser ?: return callback(null, null)
        userRepository.getById(firebaseUser.uid) {
            callback(firebaseUser.uid, it)
        }
    }

    fun getCurrentUser(callback: (String, User) -> Unit) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        firebaseUser ?: throw RuntimeException("Couldn't get current user")
        userRepository.getById(firebaseUser.uid) {
            if (it == null) {
                throw RuntimeException("Couldn't get current user")
            }
            callback(firebaseUser.uid, it)
        }
    }

    fun getOrCreateCurrentUser(callback: (String?, User?) -> Unit) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        firebaseUser ?: return callback(null, null)
        fetchCurrentUser { id, user ->
            if (user == null) {
                createFromFirebaseUser(firebaseUser, callback)
                return@fetchCurrentUser
            }
            callback(id, user)
        }
    }

    private fun createFromFirebaseUser(firebaseUser: FirebaseUser, callback: (String?, User?) -> Unit) {
        userRepository.create(
            firebaseUser.uid,
            firebaseUser.displayName ?: "",
            firebaseUser.photoUrl.toString()) {
            callback(firebaseUser.uid, it)
        }
    }
}