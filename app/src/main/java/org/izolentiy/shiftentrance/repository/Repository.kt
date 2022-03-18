package org.izolentiy.shiftentrance.repository

import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.izolentiy.shiftentrance.DATE_FORMAT
import org.izolentiy.shiftentrance.model.ExchangeRate
import java.util.*
import javax.inject.Inject

class Repository @Inject constructor(
    private val exchangeRateDao: ExchangeRatesDao,
    private val service: CbrService
) {

    private val reloadResource = MutableStateFlow<Resource<ExchangeRate?>?>(null)

    fun getExchangeRate(): Flow<Resource<ExchangeRate?>> = networkBoundResource(
        loadFromDb = { exchangeRateDao.getExchangeRates().map { rates -> rates.firstOrNull() } },
        shouldFetch = { rate -> rate == null || rate.currencies.isNullOrEmpty() },
        fetchFromNet = { fetchDailyRate() },
        saveFetchResult = { rate -> exchangeRateDao.insertExchangeRates(rate!!) }
    ).combine(reloadResource) { networkBound, reload ->
        if (reload?.status == Resource.Status.ERROR)
            Resource.error(reload.error!!, networkBound.data)
        else networkBound
    }

    suspend fun reloadDailyRate() = try {
        val result = fetchDailyRate()
        exchangeRateDao.insertExchangeRates(result)
        reloadResource.value = Resource.success(result)
    } catch (throwable: Throwable) {
        reloadResource.value = Resource.error(throwable)
    }

    suspend fun loadLatestRates(count: Int) {
        // Get latest rate. Check if it is not null, if so fetch from network latest
        // Get date of previous rate. Check if in DB exists this rate, else it is not, fetch it
        // API RESTRICTIONS: less than 5 requests per second, 120 requests per minute.
        val inLocal = exchangeRateDao.getLatestRate()
        val latestRate = inLocal ?: fetchDailyRate()
        var previousURL = latestRate.previousURL
        var previousDate = latestRate.previousDate

        val rates = mutableListOf<ExchangeRate>()
        for (i in 1..count) {
            Log.i(TAG, "loadLastRates: $i")
            val previousRate = loadPrevRate(previousDate, previousURL)
            rates.add(previousRate)

            previousURL = previousRate.previousURL
            previousDate = previousRate.previousDate
            delay(1000)  // API RESTRICTION! Make 3 api calls per second
        }
        rates.forEach { rate ->
            if (rate.date != rate.previousDate || rate.timestamp != "") {
                Log.w(TAG, "loadLatestRates: ${DATE_FORMAT.format(rate.date)}")
                exchangeRateDao.insertExchangeRates(rate)
            }
        }
    }

    private suspend fun loadPrevRate(date: Date, url: String): ExchangeRate = try {
        val formatted = DATE_FORMAT.format(date)
        Log.i(TAG, "fetchRateByUrl: FETCH_RATE_BY_URL $url $formatted")

        val rateInLocal = exchangeRateDao.getExchangeRateByDate(date)
        if (rateInLocal != null) rateInLocal
        else {
            val response = service.getExchangeByUrl(url)
            val rateFromNet = response.body()

            if (response.isSuccessful) rateFromNet!!
            else ExchangeRate()
        }
    } catch (error: Throwable) {
        Log.e(TAG, "fetchRateByUrl: ${error.message}", error)
        ExchangeRate()
    }

    private suspend fun fetchDailyRate(): ExchangeRate = try {
        val response = service.getDailyRate()
        if (response.isSuccessful) response.body()!!
        else ExchangeRate()
    } catch (error: Throwable) {
        Log.e(TAG, "fetchDailyRate: ${error.message}", error)
        ExchangeRate()
    }

    companion object {
        private val TAG = "${Repository::class.java.simpleName}_TAG"
    }

}