package com.tonyjhuang.qfit.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.tonyjhuang.qfit.data.models.User

class CurrentUserRepository(private val userRepository: UserRepository) {
    fun getCurrentUser(callback: (String?, User?) -> Unit) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        firebaseUser ?: return callback(null, null)
        userRepository.get(firebaseUser.uid) {
            callback(firebaseUser.uid, it)
        }
    }

    fun getOrCreateCurrentUser(callback: (String?, User?) -> Unit) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        firebaseUser ?: return callback(null, null)
        getCurrentUser() { id, user ->
            if (user == null) {
                createFromFirebaseUser(firebaseUser, callback)
                return@getCurrentUser
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