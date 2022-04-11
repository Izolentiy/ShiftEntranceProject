package org.izolentiy.shiftentrance.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.izolentiy.shiftentrance.API_CALL_DELAY
import org.izolentiy.shiftentrance.toTimeout
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
internal class RemoteCallManagerTest {
    private val callManager = RemoteCallManager()
    private val work = suspend {  }

    @Test
    fun `single call doesn't delay`() = runTest {
        withTimeout(timeMillis = 5) { callManager.perform(work) }
    }

    @Test
    fun `several calls delay (call count - 1) times`() = runTest {
        val callCount = 8
        val delayCount = 7 // first call doesn't delay

        withTimeout(API_CALL_DELAY.toTimeout(delayCount)) {
            repeat(callCount) { callManager.perform(work) }
        }
    }
}