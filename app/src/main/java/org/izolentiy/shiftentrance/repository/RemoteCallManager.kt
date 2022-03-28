package org.izolentiy.shiftentrance.repository

import kotlinx.coroutines.delay
import org.izolentiy.shiftentrance.API_CALL_DELAY
import org.izolentiy.shiftentrance.API_CALL_PER_SECOND

class RemoteCallManager {
    private var lastCallTime: Long = 0
    private val diffCallTime get() = System.currentTimeMillis() - lastCallTime

    /**
     * Ensures that count of calls per second to remote
     * api don't exceed limit [API_CALL_PER_SECOND]
     */
    suspend fun <T> perform(call: suspend () -> T): T {
        if (diffCallTime < API_CALL_DELAY)
            delay(API_CALL_DELAY - diffCallTime)
        lastCallTime = System.currentTimeMillis()
        return call.invoke()
    }
}