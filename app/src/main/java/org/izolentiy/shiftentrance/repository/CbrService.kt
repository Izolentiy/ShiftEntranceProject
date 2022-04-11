package org.izolentiy.shiftentrance.repository

import org.izolentiy.shiftentrance.model.ExchangeRate
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

interface CbrService {
    companion object {
        const val BASE_URL = "https://www.cbr-xml-daily.ru/"
    }

    // https://www.cbr-xml-daily.ru/daily_json.js
    @GET("daily_json.js")
    suspend fun getDailyRate(): Response<ExchangeRate>

    // https://www.cbr-xml-daily.ru/archive/2022/03/11/daily_json.js
    @GET("archive/{date}/daily_json.js")
    suspend fun getExchangeByDate(
        @Path("date") date: String
    ): Response<ExchangeRate>

    // https://www.cbr-xml-daily.ru/archive/2022/03/29/daily_json.js
    @GET
    suspend fun getExchangeByUrl(
        @Url url: String
    ): Response<ExchangeRate>

}