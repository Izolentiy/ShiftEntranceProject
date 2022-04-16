package org.izolentiy.shiftentrance.repository

sealed class Resource<out T> {

    class Success<T>(val data: T) : Resource<T>()

    class Error<T>(val error: Throwable, val data: T? = null) : Resource<T>()

    object Loading : Resource<Nothing>()

}