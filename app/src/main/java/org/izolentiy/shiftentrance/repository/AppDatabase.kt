package org.izolentiy.shiftentrance.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.izolentiy.shiftentrance.model.CurrenciesConverter
import org.izolentiy.shiftentrance.model.DateConverter
import org.izolentiy.shiftentrance.model.ExchangeRate

@Database(
    entities = [ExchangeRate::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class, CurrenciesConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exchangeRatesDao(): ExchangeRatesDao
}