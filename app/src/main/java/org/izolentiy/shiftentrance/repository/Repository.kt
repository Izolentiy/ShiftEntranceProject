package org.izolentiy.shiftentrance.repository

import android.util.Log
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import org.izolentiy.shiftentrance.DATE_FORMAT
import org.izolentiy.shiftentrance.model.ExchangeRate
import java.util.*
import javax.inject.Inject

class Repository @Inject constructor(
    private val exchangeRateDao: ExchangeRatesDao,
    private val service: CbrService
) {

    private var shouldReload = false
    private val reloadTrigger = MutableSharedFlow<Unit>(
        replay = 1, extraBufferCapacity = 0, BufferOverflow.DROP_OLDEST
    ).also { it.tryEmit(Unit) }

    val exchangeRate: Flow<Resource<out ExchangeRate?>> = flow {
        reloadTrigger.collect {
            Log.i(TAG, "exchangeRate: RELOAD TRIGGERED")
            emit(Resource.loading(null))
            emit(loadExchangeRate(shouldReload))
            shouldReload = false
            Log.i(TAG, "exchangeRate: RELOAD COMPLETED")
        }
    }.catch {
        emit(Resource.error(Throwable("Error while loading from database")))
    }

    suspend fun reloadRate() {
        shouldReload = true
        reloadTrigger.emit(Unit)
    }

    suspend fun loadLatestRates(count: Int) = try {
        // Get latest rate. Check if it is not null, if so fetch from network latest.
        val inLocal = exchangeRateDao.getLatestRate()
        val latestRate = inLocal
            ?: service.getDailyRate().body()
            ?: throw Throwable("Fetch result is null")

        // Get date of previous rate.
        var previousURL = latestRate.previousURL
        var previousDate = latestRate.previousDate

        val rates = mutableListOf<ExchangeRate>()
        repeat(count) { i ->
            Log.i(TAG, "loadLastRates: $i")
            // Check if in DB exists this rate, else if it is not, fetch it.
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
    } catch (exception: Throwable) {
        Log.e(TAG, "loadLatestRates: $exception", exception)
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

    private suspend fun loadExchangeRate(fetch: Boolean) = networkBoundResourceSus(
        loadFromDb = {
            Log.i(TAG, "loadExchangeRate: loadFromDb{}")
            exchangeRateDao.getLatestRate()
        },
        shouldFetch = { rate ->
            val predicate = rate == null || rate.currencies.isNullOrEmpty()
            Log.i(TAG, "loadExchangeRate: shouldFetch{}: $fetch or $predicate")
            fetch || predicate
        },
        fetchFromNet = {
            Log.i(TAG, "loadExchangeRate: fetchFromNet{}")
            service.getDailyRate().body()
        },
        saveFetchResult = { rate ->
            Log.i(TAG, "loadExchangeRate: saveFetchResult{}")
            if (rate != null) exchangeRateDao.insertExchangeRates(rate)
        }
    )

    companion object {
        private val TAG = "${Repository::class.java.simpleName}_TAG"
    }

}