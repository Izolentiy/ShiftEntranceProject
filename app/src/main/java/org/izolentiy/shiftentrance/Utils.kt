package org.izolentiy.shiftentrance

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.LocaleList
import android.util.TypedValue
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

const val BASE_CURRENCY = "RUB"
const val MESSAGE_TIMEOUT = 5000 // milliseconds

const val API_CALL_PER_SECOND = 3  // permitted
const val API_CALL_DELAY = (1000 / API_CALL_PER_SECOND).toLong()

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
const val BORDER_WIDTH = 0.6f

const val TOP_EXTRA_OFFSET = 12f
const val BOTTOM_EXTRA_OFFSET = 12f
const val RIGHT_EXTRA_OFFSET = 25f

@SuppressLint("ConstantLocale")
val CHART_DATE_FORMAT = SimpleDateFormat("dd MMM", Locale(Locale.getDefault().language))
val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ENGLISH)
val MESSAGE_FORMAT = SimpleDateFormat("HH:mm dd.MM.yyyy XXX", Locale.ENGLISH)

fun Float.toStringDate(): String = CHART_DATE_FORMAT.format(Date(this.toLong()))

fun resolveColor(context: Context, attr: Int): Int {
    val colorResId = TypedValue().apply {
        context.theme.resolveAttribute(attr, this, true)
    }.resourceId
    return ContextCompat.getColor(context, colorResId)
}

fun resolveDrawable(context: Context, attr: Int): Drawable? {
    val drawableResId = TypedValue().apply {
        context.theme.resolveAttribute(attr, this, true)
    }.resourceId
    return ContextCompat.getDrawable(context, drawableResId)
}