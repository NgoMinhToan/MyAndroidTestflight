package com.mycompany.testflight.domain.error

sealed class AppError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class Network(message: String, cause: Throwable? = null) : AppError(message, cause)
    class Server(message: String) : AppError(message)
    class Unknown(message: String, cause: Throwable? = null) : AppError(message, cause)
}
