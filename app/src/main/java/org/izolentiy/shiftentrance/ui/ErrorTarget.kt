package org.izolentiy.shiftentrance.ui

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import okio.IOException
import org.izolentiy.shiftentrance.R
import retrofit2.HttpException
import java.net.SocketTimeoutException

class ErrorTarget(
    val messageTarget: TextView,
    val detailTarget: TextView,
    val actionTarget: View,
    val context: Context
) {

    private fun configureErrorTarget(
        message: String, detail: String,
        showAction: Boolean = true
    ) {
        messageTarget.text = message
        detailTarget.text = detail
        actionTarget.isVisible = showAction
    }

    fun handleError(error: Throwable) = with(context) {
        when (error) {
            is HttpException -> {
                when (error.code()) {
                    401 -> configureErrorTarget(
                        message = getString(R.string.message_401),
                        detail = getString(R.string.detail_401)
                    )
                    in (400 until 500) -> configureErrorTarget(
                        message = getString(R.string.message_400_500),
                        detail = getString(R.string.detail_400_500, error.code(), error.message())
                    )
                    in (500 until 600) -> configureErrorTarget(
                        message = getString(R.string.message_500_600),
                        detail = getString(R.string.detail_500_600, error.code(), error.message()),
                        showAction = false
                    )
                    else -> configureErrorTarget(
                        message = getString(R.string.message_unexpected),
                        detail = error.message()
                    )
                }
            }
            is SocketTimeoutException -> {
                configureErrorTarget(
                    message = getString(R.string.message_socket_timeout),
                    detail = getString(R.string.detail_socket_timeout)
                )
            }
            is IOException -> configureErrorTarget(
                message = getString(R.string.message_no_internet),
                detail = getString(R.string.detail_no_internet)
            )
            else -> configureErrorTarget(
                message = getString(R.string.message_unknown),
                detail = getString(R.string.detail_unknown)
            )
        }
    }

}

