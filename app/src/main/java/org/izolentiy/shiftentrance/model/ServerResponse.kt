package org.izolentiy.shiftentrance.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "server_response")
data class ServerResponse(
    @PrimaryKey
    val date: String,
    val previousDate: String,
    val previousURL: String,
    val timestamp: String,
    @TypeConverters(CurrenciesConverter::class)
    val currencies: List<Currency>
)

data class Currency(
    val id: String,
    val numCode: String,
    val charCode: String,
    val nominal: Int,
    val name: String,
    val value: Double,
    val previous: Double
)