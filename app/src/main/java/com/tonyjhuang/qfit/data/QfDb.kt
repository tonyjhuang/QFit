package com.tonyjhuang.qfit.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class QfDb(private val db: DatabaseReference) {

    fun exists(path: String, id: String, callback: (Boolean) -> Unit) {
        val childRef = db.child(path).child(id)
        childRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                callback(false)
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    callback(true)
                    return
                }
                callback(false)
            }
        })
    }


    fun <T> create(path: String, id: String, value: T, callback: (T) -> Unit) {
        db.child(path).child(id).setValue(value)
        callback(value)
    }
}