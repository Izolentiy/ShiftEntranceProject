package org.izolentiy.shiftentrance

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.izolentiy.shiftentrance.model.Currency
import org.izolentiy.shiftentrance.model.ExchangeRate
import org.izolentiy.shiftentrance.ui.DATE_FORMAT
import retrofit2.Response
import java.util.*

val TEST_CURRENCIES = listOf(
    Currency(
        id = "R01020A", numCode = "944", charCode = "AZN", nominal = 1,
        name = "Азербайджанский манат", value = 56.411, previous = 56.6375
    ),
    Currency(
        id = "R01010", numCode = "036", charCode = "AUD", nominal = 1,
        name = "Австралийский доллар", value = 71.8611, previous = 71.9575
    ),
)

val RATE_SAVED_BEFORE = ExchangeRate(
    loaded = "2022-03-26T12:00:00+03:00".toDate(),  // 12:00 UTC+3
    currencies = TEST_CURRENCIES
)
val RATE_FROM_NET = ExchangeRate(
    loaded = "2022-03-26T15:00:00+03:00".toDate(), // 15:00 UTC+3
    currencies = TEST_CURRENCIES
)

val ERROR_BODY = """{"type": "error","message": "Nothing."}"""
    .trimMargin()
    .toResponseBody("application/json".toMediaTypeOrNull())

fun String.toDate(): Date = DATE_FORMAT.parse(this)!!

fun Long.toTimeout(delayMult: Int) = this * delayMult + 100  // wait extra 100 ms

fun <T> remoteError(): Response<T> = Response.error(403, ERROR_BODY)

fun <T> remoteSuccess(body: T): Response<T> = Response.success(body)