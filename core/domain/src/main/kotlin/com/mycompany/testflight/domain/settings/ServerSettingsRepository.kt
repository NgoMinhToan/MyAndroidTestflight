package com.mycompany.testflight.domain.settings

import kotlinx.coroutines.flow.StateFlow

interface ServerSettingsRepository {
    val serverUrl: StateFlow<String>
    val defaultServerUrl: String

    suspend fun updateServerUrl(raw: String): ServerUrlValidation
    suspend fun resetToDefault()
}

sealed interface ServerUrlValidation {
    data class Valid(val normalizedUrl: String) : ServerUrlValidation
    data class Invalid(val reason: ServerUrlError) : ServerUrlValidation
}

enum class ServerUrlError {
    Empty,
    Malformed,
    UnsupportedScheme,
    MissingHost,
    PathNotAllowed,
    PortOutOfRange,
}
