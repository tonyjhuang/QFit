package com.tonyjhuang.qfit.data.models

import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.PropertyName

@IgnoreExtraProperties
data class Group(
    val metadata: GroupMetadata? = null,
    val members: Map<String, Boolean>? = emptyMap()
)

@IgnoreExtraProperties
data class GroupMetadata(
    val name: String? = "",
    @set:PropertyName("creator_id")
    @get:PropertyName("creator_id")
    var creatorId: String? = null,
    val goals: Map<String, GroupGoal>? = emptyMap()
)