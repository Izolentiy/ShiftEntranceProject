package org.izolentiy.shiftentrance.repository

import android.util.Log
import kotlinx.coroutines.flow.*

fun <T> networkBoundResource(
    loadFromDb: () -> Flow<T>,
    shouldFetch: (T) -> Boolean = { true },
    fetchFromNet: suspend (T) -> T,
    saveFetchResult: suspend (T) -> Unit
) = flow {
    val flow = if (shouldFetch(loadFromDb().first())) {
        try {
            saveFetchResult(fetchFromNet(loadFromDb().first()))
            loadFromDb()
        } catch (exception: Throwable) {
            Log.d(TAG, "networkBoundResource: FETCHING_ERROR $exception")
            loadFromDb()
        }
    } else {
        loadFromDb()
    }
    emitAll(flow)
}

const val TAG = "NetworkBoundResource_TAG"