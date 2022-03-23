package org.izolentiy.shiftentrance.repository

data class Resource<T>(
    val status: Status,
    val data: T?,
    val error: Throwable?
) {
    enum class Status {
        SUCCESS, LOADING, ERROR
    }

    companion object {
        fun <T> success(data: T): Resource<T> =
            Resource(Status.SUCCESS, data, null)

        fun <T> loading(data: T? = null): Resource<T> =
            Resource(Status.LOADING, data, null)

        fun <T> error(error: Throwable, data: T? = null): Resource<T> =
            Resource(Status.ERROR, data, error)
    }
}