package com.tonyjhuang.qfit

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener


open class SimpleValueEventListener : ValueEventListener {
    override fun onDataChange(p0: DataSnapshot) {}

    override fun onCancelled(p0: DatabaseError) {}
}