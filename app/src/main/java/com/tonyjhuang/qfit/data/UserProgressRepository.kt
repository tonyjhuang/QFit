package com.tonyjhuang.qfit.data

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class UserProgressRepository(private val db: DatabaseReference) {

    fun watchDailyProgress(
        userId: String,
        today: Date,
        listener: ValueEventListener
    ) {
        db.child("$PATH/$userId/${today.toIso()}")
            .addValueEventListener(listener)
    }

    fun unwatchDailyProgress(
        userId: String,
        today: Date,
        listener: ValueEventListener
    ) {
        db.child("$PATH/$userId/${today.toIso()}")
            .removeEventListener(listener)
    }

    fun addProgress(
        userId: String,
        goalId: String,
        today: Date,
        newProgressAmount: Int
    ) {
        db.child("$PATH/$userId/${today.toIso()}/$goalId/amount").setValue(newProgressAmount)
    }


    companion object {
        const val PATH = "user_progress"
    }
}

fun Date.toIso() = SimpleDateFormat("yyyyMMdd", Locale.US).format(this)
