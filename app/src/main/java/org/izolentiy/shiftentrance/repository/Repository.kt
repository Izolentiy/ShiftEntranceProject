package org.izolentiy.shiftentrance.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.izolentiy.shiftentrance.model.ExchangeRate
import javax.inject.Inject

class Repository @Inject constructor(
    private val exchangeRateDao: ExchangeRatesDao,
    private val service: CbrService
) {

    fun getExchangeRate(): Flow<ExchangeRate?> = networkBoundResource(
        loadFromDb = {
            exchangeRateDao.getExchangeRates().map { rates -> rates.firstOrNull() }
        },
        shouldFetch = { rate -> rate == null || rate.currencies.isNullOrEmpty() },
        fetchFromNet = { fetchDailyRate() },
        saveFetchResult = { rate -> exchangeRateDao.insertExchangeRates(rate!!) }
    )

    suspend fun reloadDailyRate() {
        exchangeRateDao.insertExchangeRates(fetchDailyRate())
    }

    private suspend fun fetchDailyRate(): ExchangeRate {
        val response = service.getDailyRate()
        return if (response.isSuccessful) {
            Log.d(TAG, "fetchDailyRate: DAILY_RATE_FETCHED")
            response.body()!!
        } else {
            Log.d(TAG, "fetchDailyRate: DAILY_RATE_WAS_NOT_FETCHED")
            ExchangeRate()
        }
    }

    companion object {
        private val TAG = "${Repository::class.java.simpleName}_TAG"
    }

}