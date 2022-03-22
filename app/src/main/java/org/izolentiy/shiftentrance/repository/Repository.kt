package org.izolentiy.shiftentrance.repository

import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import org.izolentiy.shiftentrance.DATE_FORMAT
import org.izolentiy.shiftentrance.model.ExchangeRate
import java.util.*
import javax.inject.Inject
import kotlin.contracts.ContractBuilder

class Repository @Inject constructor(
    private val exchangeRateDao: ExchangeRatesDao,
    private val service: CbrService
) {

    private var shouldReload = false
    private val loadingTrigger = MutableSharedFlow<Unit>(replay = 1).also { it.tryEmit(Unit) }

    val exchangeRate: Flow<Resource<out ExchangeRate?>> = flow {
        loadingTrigger.collect {
            Log.w(TAG, "exchangeRate: LOADING TRIGGERED")
            emit(Resource.loading(null))
            emit(loadExchangeRate(shouldReload))
            shouldReload = false
            Log.w(TAG, "exchangeRate: LOADING COMPLETED")
        }
    }.catch {
        emit(Resource.error(Throwable("Error while loading from database")))
    }

    suspend fun reloadRate() {
        shouldReload = true
        loadingTrigger.emit(Unit)
    }

    suspend fun loadLatestRates(count: Int): List<ExchangeRate>? = try {
        // Get latest rate. Check if it is not null, if so fetch from network latest.
        val inLocal = exchangeRateDao.getLatestRate()
        val latestRate = inLocal
            ?: service.getDailyRate().body()
            ?: throw Throwable("Fetch result is null")

        // Get date of previous rate.
        var previousURL = latestRate.previousURL
        var previousDate = latestRate.previousDate

        val rates = mutableListOf<ExchangeRate>()
        rates.add(latestRate)
        repeat(count - 1) { i ->
            Log.i(TAG, "loadLastRates: $i")
            // Check if in DB exists this rate, else if it is not, fetch it.
            val previousRate = loadPrevRate(previousDate, previousURL)
                ?: throw Throwable("Loading previous rate returned null")
            rates.add(previousRate)
            Log.i(TAG, "loadLatestRates: ${DATE_FORMAT.format(previousRate.date)}")

            previousURL = previousRate.previousURL
            previousDate = previousRate.previousDate
        }
        rates
    } catch (exception: Throwable) {
        Log.e(TAG, "loadLatestRates: ${exception.message}", exception)
        null
    }

    private suspend fun loadPrevRate(date: Date, url: String): ExchangeRate? {
        val formatted = DATE_FORMAT.format(date)
        Log.i(TAG, "loadPrevRate: url = $url, date = $formatted")
        val result = exchangeRateDao.getExchangeRateByDate(date) ?:
            service.getExchangeByUrl(url).body().also { rate ->
                if (rate != null) exchangeRateDao.insertExchangeRates(rate)
                delay(340)  // API RESTRICTION! Make 3 api calls per second
            }
        return result
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