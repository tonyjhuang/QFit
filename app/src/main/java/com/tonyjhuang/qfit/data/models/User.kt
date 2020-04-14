package com.tonyjhuang.qfit.data.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    val name: String? = "",
    val photo_url: String? = "",
    val groups: Map<String, Boolean>? = emptyMap()
)
