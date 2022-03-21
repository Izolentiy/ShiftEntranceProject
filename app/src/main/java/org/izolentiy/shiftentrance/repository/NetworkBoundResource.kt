package org.izolentiy.shiftentrance.repository

import android.util.Log

suspend fun <T> networkBoundResourceSus(
    loadFromDb: () -> T,
    shouldFetch: (T) -> Boolean = { true },
    fetchFromNet: suspend (T) -> T,
    saveFetchResult: suspend (T) -> Unit
): Resource<T> {
    val dataFromDb = loadFromDb()
    return if (shouldFetch(dataFromDb)) {
        try {
            val dataFromNet = fetchFromNet(dataFromDb) ?: throw Throwable("Empty fetch result")
            saveFetchResult(dataFromNet)
            Log.d(TAG, "networkBoundResource: FETCHED_AND_LOADED")
            Resource.success(dataFromNet)
        } catch (exception: Throwable) {
            Log.e(TAG, "networkBoundResource: FETCHING_ERROR $exception")
            Resource.error(exception, dataFromDb)
        }
    } else {
        Log.d(TAG, "networkBoundResource: NO_NEED_TO_FETCH")
        Resource.success(dataFromDb)
    }
}

const val TAG = "NetworkBoundResource_TAG"