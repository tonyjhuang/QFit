package com.tonyjhuang.qfit.data

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.tonyjhuang.qfit.toIso
import java.util.*

class GroupProgressRepository(private val db: DatabaseReference) {

    fun watchDailyProgress(
        groupId: String,
        today: Date,
        listener: ValueEventListener
    ) {
        db.child("$PATH/$groupId/${today.toIso()}")
            .addValueEventListener(listener)
    }

    companion object {
        const val PATH = "group_progress"
    }
}