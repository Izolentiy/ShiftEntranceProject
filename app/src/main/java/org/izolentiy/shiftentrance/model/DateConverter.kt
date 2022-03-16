package org.izolentiy.shiftentrance.model

import androidx.room.TypeConverter
import org.izolentiy.shiftentrance.DATE_FORMAT
import java.text.SimpleDateFormat
import java.util.*

class DateConverter {

    @TypeConverter
    fun toDate(json: String): Date {
        return DATE_FORMAT.parse(json)!!
    }

    @TypeConverter
    fun fromDate(date: Date): String {
        return DATE_FORMAT.format(date)
    }
}