package com.tonyjhuang.qfit.data.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Group(
    val members: MutableMap<String, Boolean>?,
    val goals: MutableMap<String, GroupGoal>?)

@IgnoreExtraProperties
data class GroupGoal(val amount: Int)