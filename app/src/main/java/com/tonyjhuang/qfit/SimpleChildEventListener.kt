package com.tonyjhuang.qfit

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError


open class SimpleChildEventListener : ChildEventListener {
    override fun onCancelled(p0: DatabaseError) {}

    override fun onChildMoved(p0: DataSnapshot, p1: String?) {}

    override fun onChildChanged(p0: DataSnapshot, p1: String?) {}

    override fun onChildAdded(p0: DataSnapshot, p1: String?) {}

    override fun onChildRemoved(p0: DataSnapshot) {}
}