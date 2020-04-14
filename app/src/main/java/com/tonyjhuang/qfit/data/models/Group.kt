package com.tonyjhuang.qfit.data.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Group(
    val name: String? = "",
    val members: Map<String, Boolean>? = emptyMap(),
    val goals: Map<String, GroupGoal>? = emptyMap())

@IgnoreExtraProperties
data class GroupGoal(val amount: Int)