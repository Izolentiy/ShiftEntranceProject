package org.izolentiy.shiftentrance

import java.text.SimpleDateFormat
import java.util.*

const val BASE_CURRENCY = "RUB"
const val MESSAGE_TIMEOUT = 5000 // milliseconds
const val LABEL_COUNT_X_AXIS = 7
const val LABEL_COUNT_Y_AXIS = 14

// Chart dimensions
const val Y_OFFSET = 12f
const val X_OFFSET = 16f
const val CHART_TEXT_SIZE = 12f
const val NO_DATA_TEXT_SIZE = 50f

const val DETAIL_MARKER_OFFSET_MULTIPLIER = 1.2f

// TODO: Make it work with xml
const val CIRCLE_RADIUS = 5f
const val CIRCLE_HOLE_RADIUS = 3f
const val LINE_WIDTH = 3f
const val HIGHLIGHT_LINE_WIDTH = 1f
const val BORDER_WIDTH = 0.6f

const val TOP_EXTRA_OFFSET = 12f
const val BOTTOM_EXTRA_OFFSET = 12f
const val RIGHT_EXTRA_OFFSET = 25f

val CHART_DATE_FORMAT = SimpleDateFormat("dd MMM", Locale.ENGLISH)
val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ENGLISH)
val MESSAGE_FORMAT = SimpleDateFormat("HH:mm dd.MM.yyyy XXX", Locale.ENGLISH)

fun Float.toStringDate(): String = CHART_DATE_FORMAT.format(Date(this.toLong()))