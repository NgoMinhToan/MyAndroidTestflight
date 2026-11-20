package com.mycompany.testflight.domain.error

sealed class AppError(cause: Throwable? = null) : Exception(cause) {
    class Network(cause: Throwable? = null) : AppError(cause)
    data class Server(val code: Int) : AppError()
    class InvalidServerUrl(cause: Throwable? = null) : AppError(cause)
    class Unexpected(cause: Throwable? = null) : AppError(cause)
}
