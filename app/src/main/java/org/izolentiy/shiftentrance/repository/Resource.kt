package org.izolentiy.shiftentrance.repository

sealed class Resource<out T> {

    data class Success<T>(val data: T) : Resource<T>()

    data class Error<T>(val error: Throwable, val data: T? = null) : Resource<T>()

    object Loading : Resource<Nothing>()

}