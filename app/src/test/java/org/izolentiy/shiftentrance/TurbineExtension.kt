package org.izolentiy.shiftentrance

import app.cash.turbine.FlowTurbine
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.coroutineContext

/**
 * Make Turbine for testing flows work with new API [runTest] that ignore delays.
 * Inside of Turbine is used [withTimeout] API, that exceeds instantly, when flow collection
 * started inside [runTest], and if flow that we test use [delay] inside, it can cause
 * [TimeoutCancellationException]
 * @see <a href="https://github.com/cashapp/turbine/issues/42#issuecomment-1000317026">
 *     PaulWoitaschek's comment about this workaround
 *     </a>
 */
@ExperimentalCoroutinesApi
suspend fun <T> Flow<T>.testFlow(
    timeout: Long = 1000L,
    validate: suspend FlowTurbine<T>.() -> Unit,
) {
    val testScheduler = coroutineContext[TestCoroutineScheduler]
    return if (testScheduler == null) {
        test(timeout, validate)
    } else {
        flowOn(UnconfinedTestDispatcher(testScheduler))
            .test(timeout, validate)
    }
}