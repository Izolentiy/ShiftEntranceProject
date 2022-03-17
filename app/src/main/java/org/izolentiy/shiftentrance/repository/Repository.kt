package org.izolentiy.shiftentrance.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.izolentiy.shiftentrance.model.ExchangeRate
import javax.inject.Inject

class Repository @Inject constructor(
    private val exchangeRateDao: ExchangeRatesDao,
    private val service: CbrService
) {

    private val reloadErrorFlow = MutableStateFlow<Resource<ExchangeRate?>?>(null)

    fun getExchangeRate(): Flow<Resource<ExchangeRate?>> = networkBoundResource(
        loadFromDb = {
            Log.i(TAG, "getExchangeRate: loadFromDb")
            exchangeRateDao.getExchangeRates().map { rates -> rates.firstOrNull() }
        },
        shouldFetch = { rate ->
            val predicate = rate == null || rate.currencies.isNullOrEmpty()
            Log.i(TAG, "getExchangeRate: shouldFetch $predicate")
            rate == null || rate.currencies.isNullOrEmpty()
        },
        fetchFromNet = {
            Log.i(TAG, "getExchangeRate: fetchFromNet")
            fetchDailyRate()
        },
        saveFetchResult = { rate ->
            Log.i(TAG, "getExchangeRate: saveFetchResult")
            exchangeRateDao.insertExchangeRates(rate!!)
        }
    ).combine(reloadErrorFlow) { boundResource, reload ->
        Log.d(TAG, "getExchangeRate: COMBINATION_START")
        if (reload?.status == Resource.Status.ERROR) {
            Log.d(TAG, "getExchangeRate: RELOAD_ERROR")
            Resource.error(reload.error!!, boundResource.data)
        } else {
            when (boundResource.status) {
                Resource.Status.LOADING -> {
                    Log.d(TAG, "getExchangeRate: LOADING")
                    Resource.loading(boundResource.data)
                }
                Resource.Status.SUCCESS -> {
                    Log.d(TAG, "getExchangeRate: SUCCESS")
                    Resource.success(boundResource.data)
                }
                Resource.Status.ERROR -> {
                    Log.d(TAG, "getExchangeRate: ERROR")
                    Resource.error(boundResource.error!!, boundResource.data)
                }
            }
        }
    }

    suspend fun reloadDailyRate() = try {
        val result = fetchDailyRate()
        exchangeRateDao.insertExchangeRates(result)
        reloadErrorFlow.value = Resource.success(null)
    } catch (throwable: Throwable) {
        reloadErrorFlow.value = Resource.error(throwable)
    }

    private suspend fun fetchDailyRate(): ExchangeRate {
        val response = service.getDailyRate()
        return if (response.isSuccessful) response.body()!!
        else ExchangeRate()
    }

    companion object {
        private val TAG = "${Repository::class.java.simpleName}_TAG"
    }

}