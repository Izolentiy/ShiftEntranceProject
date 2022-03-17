package org.izolentiy.shiftentrance.repository

import android.util.Log
import kotlinx.coroutines.flow.*

fun <T> networkBoundResource(
    loadFromDb: () -> Flow<T>,
    shouldFetch: (T) -> Boolean = { true },
    fetchFromNet: suspend (T) -> T,
    saveFetchResult: suspend (T) -> Unit
) = flow {
    loadFromDb().collect { data ->
        val flow: Flow<Resource<T>> = if (shouldFetch(data)) {
            emit(Resource.loading(data))
            Log.d(TAG, "networkBoundResource: LOADING")
            try {
                saveFetchResult(fetchFromNet(data))
                Log.d(TAG, "networkBoundResource: FETCHED_AND_LOADED")
                flowOf(Resource.success(data))
            } catch (throwable: Throwable) {
                Log.e(TAG, "networkBoundResource: FETCHING_ERROR $throwable")
                flowOf(Resource.error(throwable, data))
            }
        } else {
            Log.d(TAG, "networkBoundResource: NO_NEED_TO_FETCH")
            flowOf(Resource.success(data))
        }
        emitAll(flow)
    }
}

const val TAG = "NetworkBoundResource_TAG"