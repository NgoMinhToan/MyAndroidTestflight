package dev.minhtoan.mytestflight.data.settings

import dev.minhtoan.mytestflight.domain.settings.ServerSettingsRepository
import dev.minhtoan.mytestflight.domain.settings.ServerUrlValidation
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class ServerSettingsRepositoryImpl @Inject constructor(
    private val store: ServerUrlStore,
    private val validator: HttpUrlServerUrlValidator,
    @Named("defaultServerUrl") defaultServerUrl: String,
) : ServerSettingsRepository {

    override val defaultServerUrl: String = defaultServerUrl.trim().trimEnd('/')

    private val _serverUrl = MutableStateFlow(readInitial())
    override val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    override suspend fun updateServerUrl(raw: String): ServerUrlValidation {
        val validation = validator.validate(raw)
        if (validation is ServerUrlValidation.Valid) {
            store.write(validation.normalizedUrl)
            _serverUrl.value = validation.normalizedUrl
        }
        return validation
    }

    override suspend fun resetToDefault() {
        store.clear()
        _serverUrl.value = defaultServerUrl
    }

    private fun readInitial(): String {
        val stored = store.read()
        val validation = stored?.let(validator::validate)
        return (validation as? ServerUrlValidation.Valid)?.normalizedUrl ?: defaultServerUrl
    }
}
