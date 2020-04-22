package com.tonyjhuang.qfit

import java.util.*

object QTime {

    val tz = TimeZone.getTimeZone("UTC");

    val today
    get() = Calendar.getInstance(tz).time
}