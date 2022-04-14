package org.izolentiy.shiftentrance.repository

import kotlinx.coroutines.delay
import org.izolentiy.shiftentrance.API_CALL_DELAY
import org.izolentiy.shiftentrance.API_CALL_PER_SECOND
import retrofit2.HttpException
import retrofit2.Response

class RemoteCallManager {
    private var lastCallTime: Long = 0
    private val diffCallTime get() = System.currentTimeMillis() - lastCallTime

    /**
     * Ensures that count of calls per second to remote
     * api don't exceed limit [API_CALL_PER_SECOND]
     */
    suspend fun <T> perform(call: suspend () -> Response<T>): T {
        if (diffCallTime < API_CALL_DELAY)
            delay(API_CALL_DELAY - diffCallTime)
        lastCallTime = System.currentTimeMillis()

        val response = call.invoke()
        if (response.isSuccessful)
            return response.body()
                ?: throw IllegalStateException("Fetch result is null")
        else throw HttpException(response)
    }

}