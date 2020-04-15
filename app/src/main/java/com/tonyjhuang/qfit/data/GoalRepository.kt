package com.tonyjhuang.qfit.data

import com.google.firebase.database.*
import com.tonyjhuang.qfit.data.models.Goal

class GoalRepository(
    private val db: DatabaseReference
) {
    fun getAll(callback: (Map<String, Goal>) -> Unit) {
        db.child(PATH).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                callback(emptyMap())
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (!p0.exists()) return callback(emptyMap())
                callback(
                    p0.getValue(object : GenericTypeIndicator<Map<String, Goal>>() {}) ?: emptyMap()
                )
            }
        })
    }

    companion object {
        const val PATH = "goals"
    }
}