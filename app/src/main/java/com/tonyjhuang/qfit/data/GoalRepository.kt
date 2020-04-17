package com.tonyjhuang.qfit.data

import com.google.firebase.database.*
import com.tonyjhuang.qfit.data.models.Goal

class GoalRepository(private val db: DatabaseReference) {
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

    fun getById(id: String, callback: (Goal?) -> Unit) {
        db.child("$PATH/$id")
            .addListenerForSingleValueEvent(GoalValueListener { _, Goal ->
                callback(Goal)
            })
    }

    fun getByIds(ids: Collection<String>, callback: (Map<String, Goal>) -> Unit) {
        val results = mutableMapOf<String, Goal>()
        var remaining = ids.size

        for (id in ids) {
            getById(id) {
                if (it != null) {
                    results[id] = it
                }
                remaining--
                if (remaining == 0) {
                    callback(results)
                }
            }
        }
    }

    class GoalValueListener(private val callback: (String?, Goal?) -> Unit) : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
            callback(null, null)
        }

        override fun onDataChange(p0: DataSnapshot) {
            if (p0.exists()) {
                callback(p0.key, p0.getValue(Goal::class.java))
                return
            }
            callback(null, null)
        }
    }


    companion object {
        const val PATH = "goals"
    }
}