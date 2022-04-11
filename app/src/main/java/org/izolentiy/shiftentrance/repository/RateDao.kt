package org.izolentiy.shiftentrance.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.izolentiy.shiftentrance.model.ExchangeRate
import java.util.*

@Dao
interface RateDao {

    @Query("SELECT * FROM exchange_rates WHERE :date = date")
    suspend fun getExchangeRateByDate(date: Date): ExchangeRate?

    @Query("SELECT * FROM exchange_rates ORDER BY date DESC LIMIT 1")
    suspend fun getLatestRate(): ExchangeRate?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExchangeRates(rate: ExchangeRate)

}