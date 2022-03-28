package org.izolentiy.shiftentrance.repository

import android.util.Log
import kotlinx.coroutines.flow.*
import org.izolentiy.shiftentrance.CHART_DATE_FORMAT
import org.izolentiy.shiftentrance.model.ExchangeRate
import java.util.*
import javax.inject.Inject

class RateRepository @Inject constructor(
    private val rateDao: ExchangeRatesDao,
    private val rateService: CbrService,
) {

    private var shouldReload = false
    private val loadingTrigger = MutableSharedFlow<Unit>(replay = 1).also { it.tryEmit(Unit) }
    private val ratesToLoad = MutableStateFlow(0)

    private val callManager = RemoteCallManager()

    val exchangeRate: Flow<Resource<out ExchangeRate?>> = flow {
        loadingTrigger.collect {
            Log.w(TAG, "exchangeRate: LOADING TRIGGERED")
            emit(Resource.loading())
            emit(loadRate(shouldReload))
            shouldReload = false
            Log.w(TAG, "exchangeRate: LOADING COMPLETED")
        }
    }
    val latestRates: Flow<Resource<List<ExchangeRate>?>> = flow {
        ratesToLoad.filter { it > 0 }.collect { count ->
            Log.w(TAG, "latestRates: LOADING LATEST $count RATES")
            emit(Resource.loading())
            emit(loadLatestRates(count))
            Log.w(TAG, "latestRates: LOADING OF $count RATES COMPLETED")
        }
    }

    fun loadRates(count: Int) {
        ratesToLoad.value = count
    }

    fun reloadRate() {
        shouldReload = true
        loadingTrigger.tryEmit(Unit)
    }

    private suspend fun loadRate(fetch: Boolean): Resource<ExchangeRate> = try {
        val dataFromDb = rateDao.getLatestRate()

        if (fetch || dataFromDb == null || dataFromDb.currencies.isEmpty()) {
            val dataFromNet = rateService.getDailyRate().body()
                ?: throw Throwable("Empty fetch result")
            rateDao.insertExchangeRates(dataFromNet)

            Log.d(TAG, "loadExchangeRate: FETCHED_AND_SAVED")
            Resource.success(dataFromNet)
        } else {
            Log.d(TAG, "loadExchangeRate: NO_NEED_TO_FETCH")
            Resource.success(dataFromDb)
        }
    } catch (exception: Throwable) {
        val dataFromDb = rateDao.getLatestRate()
        Resource.error(exception, dataFromDb)
    }

    private suspend fun loadLatestRates(count: Int): Resource<List<ExchangeRate>?> = try {
        // Get latest rate. Check if it is not null, if so fetch from network latest.
        val latestRate = loadLatestRate()
            ?: throw Throwable("Fetch result is null")

        // Get date of previous rate.
        var previousURL = latestRate.previousURL
        var previousDate = latestRate.previousDate

        val rates = mutableListOf<ExchangeRate>()
        rates.add(latestRate)
        var date = CHART_DATE_FORMAT.format(latestRate.date)
        Log.i(TAG, "loadLatestRates: 0 $date")

        repeat(count - 1) { i ->
            // Check if in DB exists this rate, else if it is not, fetch it.
            val previousRate = loadPrevRate(previousDate, previousURL)
                ?: throw Throwable("Loading previous rate returned null")

            rates.add(previousRate)
            date = CHART_DATE_FORMAT.format(previousRate.date)
            Log.i(TAG, "loadLatestRates: ${i + 1} $date")

            previousURL = previousRate.previousURL
            previousDate = previousRate.previousDate
        }
        Resource.success(rates)
    } catch (exception: Throwable) {
        Resource.error(exception)
    }

    private suspend fun loadPrevRate(date: Date, url: String): ExchangeRate? {
        return rateDao.getExchangeRateByDate(date)
            ?: callManager.perform {
                rateService.getExchangeByUrl(url).body()
                    ?.also { rateDao.insertExchangeRates(it) }
            }
    }

    private suspend fun loadLatestRate(): ExchangeRate? {
        return rateDao.getLatestRate()
            ?: callManager.perform {
                rateService.getDailyRate().body()
                    ?.also { rateDao.insertExchangeRates(it) }
            }
    }

    companion object {
        private val TAG = "${RateRepository::class.java.simpleName}_TAG"
    }

}