package org.izolentiy.shiftentrance.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.izolentiy.shiftentrance.model.ExchangeRate

@Dao
interface ExchangeRatesDao {

    @Query("SELECT * FROM exchange_rates WHERE :date = date")
    fun getExchangeRateByDate(date: String): Flow<ExchangeRate>

    @Query("SELECT * FROM exchange_rates ORDER BY date DESC")
    fun getExchangeRates(): Flow<List<ExchangeRate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExchangeRates(rate: ExchangeRate)

}