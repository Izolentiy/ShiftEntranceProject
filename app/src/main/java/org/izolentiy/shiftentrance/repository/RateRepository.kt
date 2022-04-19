package org.izolentiy.shiftentrance.repository

import android.util.Log
import kotlinx.coroutines.flow.*
import okio.IOException
import org.izolentiy.shiftentrance.model.ExchangeRate
import org.izolentiy.shiftentrance.ui.CHART_DATE_FORMAT
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.util.*
import javax.inject.Inject
import kotlin.system.measureTimeMillis

class RateRepository @Inject constructor(
    private val rateDao: RateDao,
    private val rateService: CbrService,
) {

    private var shouldReload = false
    private val loadingTrigger = MutableSharedFlow<Unit>(replay = 1).also { it.tryEmit(Unit) }
    private val ratesToLoad = MutableSharedFlow<Int>(replay = 1)

    private val callManager = RemoteCallManager()

    val exchangeRate: Flow<Resource<ExchangeRate>> = flow {
        loadingTrigger.collect {
            Log.w(TAG, "exchangeRate: LOADING TRIGGERED")
            measureTimeMillis {
                emit(Resource.Loading)
                emit(loadRate(shouldReload))
                shouldReload = false
            }.also { Log.w(TAG, "exchangeRate: LOADING COMPLETED $it ms") }
        }
    }
    val latestRates: Flow<Resource<List<ExchangeRate>?>> = flow {
        ratesToLoad.filter { it > 0 }.collect { count ->
            Log.w(TAG, "latestRates: LOADING LATEST $count RATES")
            measureTimeMillis {
                emit(Resource.Loading)
                emit(loadLatestRates(count))
            }.also { Log.w(TAG, "latestRates: LOADING OF $count RATES COMPLETED $it ms") }
        }
    }

    fun loadRates(count: Int) {
        ratesToLoad.tryEmit(count)
    }

    fun reloadRate() {
        shouldReload = true
        loadingTrigger.tryEmit(Unit)
    }

    private suspend fun loadRate(fetch: Boolean): Resource<ExchangeRate> = try {
        val dataFromDb = rateDao.getLatestRate()

        if (fetch || dataFromDb == null || dataFromDb.currencies.isEmpty()) {
            val dataFromNet = callManager.perform { rateService.getDailyRate() }
            rateDao.insertExchangeRates(dataFromNet)

            Log.d(TAG, "loadExchangeRate: FETCHED_AND_SAVED")
            Resource.Success(dataFromNet)
        } else {
            Log.d(TAG, "loadExchangeRate: NO_NEED_TO_FETCH")
            Resource.Success(dataFromDb)
        }
    } catch (exception: Throwable) {
        handleException(exception)
        val dataFromDb = rateDao.getLatestRate()
        Resource.Error(exception, dataFromDb)
    }

    private suspend fun loadLatestRates(count: Int): Resource<List<ExchangeRate>?> = try {
        val startTime = System.currentTimeMillis()
        var endTime: Long
        var date: String

        // Get latest rate. Check if it is not null, if so fetch from network latest.
        val latestRate = loadLatestRate()

        // Get date of previous rate.
        var previousURL = latestRate.previousURL
        var previousDate = latestRate.previousDate

        val rates = mutableListOf<ExchangeRate>()

        rates.add(latestRate)
        date = CHART_DATE_FORMAT.format(latestRate.date)
        endTime = System.currentTimeMillis() - startTime
        Log.i(TAG, "loadLatestRates: 0 $date $endTime ms")

        repeat(count - 1) { i ->
            // Check if in DB exists this rate, else if it is not, fetch it.
            val previousRate = loadPrevRate(previousDate, previousURL)

            rates.add(previousRate)
            date = CHART_DATE_FORMAT.format(previousRate.date)
            endTime = System.currentTimeMillis() - startTime
            Log.i(TAG, "loadLatestRates: ${i + 1} $date $endTime ms")

            previousURL = previousRate.previousURL
            previousDate = previousRate.previousDate
        }
        Resource.Success(rates)
    } catch (exception: Throwable) {
        handleException(exception)
        Resource.Error(exception)
    }

    private suspend fun loadPrevRate(date: Date, url: String): ExchangeRate {
        return rateDao.getExchangeRateByDate(date)
            ?: callManager.perform {
                rateService.getExchangeByUrl(url)
            }.also { rateDao.insertExchangeRates(it) }
    }

    private suspend fun loadLatestRate(): ExchangeRate {
        return rateDao.getLatestRate()
            ?: callManager.perform {
                rateService.getDailyRate()
            }.also { rateDao.insertExchangeRates(it) }
    }

    private fun handleException(exception: Throwable) {
        when (exception) {
            is IllegalArgumentException ->
                Log.e(TAG, "loadLatestRates: Http 2xx but fetch result is null")
            is SocketTimeoutException ->
                Log.e(TAG, "loadLatestRates: Timeout exception")
            is HttpException ->
                Log.e(TAG, "loadLatestRates: Http non 2xx status code")
            is IOException ->
                Log.e(TAG, "loadLatestRates: Network error")
            else -> throw exception
        }
    }

    companion object {
        private val TAG = "${RateRepository::class.java.simpleName}_TAG"
    }

}