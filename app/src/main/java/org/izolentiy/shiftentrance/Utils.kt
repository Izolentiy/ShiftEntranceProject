package org.izolentiy.shiftentrance

import java.text.SimpleDateFormat
import java.util.*

const val BASE_CURRENCY = "RUB"
val CHART_DATE_FORMAT = SimpleDateFormat("dd MMM", Locale.ENGLISH)
val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ENGLISH)
val MESSAGE_FORMAT = SimpleDateFormat("HH:mm dd.MM.yyyy XXX", Locale.ENGLISH)
const val MESSAGE_TIMEOUT = 5000 // milliseconds

fun Float.toStringDate(): String = CHART_DATE_FORMAT.format(Date(this.toLong()))