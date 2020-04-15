package com.tonyjhuang.qfit.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class QfDb(private val db: DatabaseReference) {

    fun exists(path: String, id: String, callback: (Boolean) -> Unit) {
        db.child(path)
            .child(id)
            .addListenerForSingleValueEvent(existsListener(callback))
    }

    fun stringValueExists(path: String, key: String, value: String, callback: (Boolean) -> Unit) {
        db.child(path)
            .orderByChild(key)
            .equalTo(value)
            .addListenerForSingleValueEvent(existsListener(callback))
    }

    fun existsListener(callback: (Boolean) -> Unit): ValueEventListener{
        return object : ValueEventListener {
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
        }
    }

    fun <T> get(path: String, id: String, callback: (T?) -> Unit) {
        db.child(path)
            .child(id)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    callback(null)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        callback(p0.value as T)
                        return
                    }
                    callback(null)
                }
            })
    }

    fun <T> create(path: String, id: String, value: T, callback: (String, T) -> Unit) {
        db.child(path).child(id).setValue(value)
        callback(id, value)
    }

    fun <T> create(path: String, value: T, callback: (String, T) -> Unit) {
        val newRef = db.child(path).push()
        newRef.setValue(value)
        callback(newRef.key!!, value)
    }
}