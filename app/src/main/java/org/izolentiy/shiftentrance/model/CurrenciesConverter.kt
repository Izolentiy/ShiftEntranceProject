package org.izolentiy.shiftentrance.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CurrenciesConverter {
    private val gson = Gson()
    private val typeToken = object : TypeToken<List<Currency>>() {}.type

    @TypeConverter
    fun toCurrencies(json: String): List<Currency> {
        return gson.fromJson(json, typeToken)
    }

    @TypeConverter
    fun fromCurrencies(currencies: List<Currency>): String {
        return gson.toJson(currencies)
    }

}