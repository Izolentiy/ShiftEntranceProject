package org.izolentiy.shiftentrance.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.*

@Entity(tableName = "exchange_rates")
data class ExchangeRate(
    @PrimaryKey
    val date: Date = Date(),
    val previousDate: String = "",
    val previousURL: String = "",
    val timestamp: String = "",
    val currencies: List<Currency> = emptyList()
)

data class Currency(
    val id: String = "",
    val numCode: String = "",
    val charCode: String = "",
    val nominal: Int = 0,
    val name: String = "",
    val value: Double = 0.0,
    val previous: Double = 0.0
)