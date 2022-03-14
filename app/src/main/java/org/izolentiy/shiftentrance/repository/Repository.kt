package org.izolentiy.shiftentrance.repository

import kotlinx.coroutines.coroutineScope
import org.izolentiy.shiftentrance.model.Currency
import javax.inject.Inject

class Repository @Inject constructor(
    private val service: CbrService
) {

    suspend fun fetchCurrencies(): List<Currency> = coroutineScope {
        val response = service.getDailyExchange()
        return@coroutineScope if (response.isSuccessful) {
            response.body()!!.currencies
        } else listOf()
    }

}