package com.tonyjhuang.qfit

import java.text.SimpleDateFormat
import java.util.*


fun Date.toIso() = SimpleDateFormat("yyyyMMdd", Locale.US).apply {
    timeZone = QTime.tz
}.format(this)
