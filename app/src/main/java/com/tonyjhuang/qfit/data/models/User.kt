package com.tonyjhuang.qfit.data.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    var name: String? = "",
    var photo_url: String? = "",
    var groups: MutableMap<String, Boolean>? = HashMap()
)
