package dev.minhtoan.mytestflight.data.settings

import dev.minhtoan.mytestflight.domain.settings.ServerUrlError
import dev.minhtoan.mytestflight.domain.settings.ServerUrlValidation
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

@Singleton
class HttpUrlServerUrlValidator @Inject constructor() {

    fun validate(raw: String): ServerUrlValidation {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return invalid(ServerUrlError.Empty)

        val scheme = SCHEME_PATTERN.find(trimmed)?.groupValues?.get(1)?.lowercase(Locale.ROOT)
            ?: return invalid(ServerUrlError.Malformed)
        if (scheme != "http" && scheme != "https") {
            return invalid(ServerUrlError.UnsupportedScheme)
        }

        PORT_PATTERN.find(trimmed)?.groupValues?.get(1)?.let { portValue ->
            val port = portValue.toIntOrNull()
            if (port == null || port !in 1..65535) {
                return invalid(ServerUrlError.PortOutOfRange)
            }
        }

        val url = trimmed.toHttpUrlOrNull() ?: return invalid(ServerUrlError.Malformed)
        if (url.host.isBlank()) return invalid(ServerUrlError.MissingHost)
        if (url.username.isNotEmpty() || url.password.isNotEmpty()) {
            return invalid(ServerUrlError.Malformed)
        }
        if (url.encodedPath != "/" || url.query != null || url.fragment != null) {
            return invalid(ServerUrlError.PathNotAllowed)
        }
        if (url.port !in 1..65535) return invalid(ServerUrlError.PortOutOfRange)

        return ServerUrlValidation.Valid(url.toString().trimEnd('/'))
    }

    private fun invalid(error: ServerUrlError): ServerUrlValidation =
        ServerUrlValidation.Invalid(error)

    private companion object {
        val SCHEME_PATTERN = Regex("^([A-Za-z][A-Za-z0-9+.-]*):")
        val PORT_PATTERN = Regex("^[A-Za-z][A-Za-z0-9+.-]*://[^/?#]*:(\\d+)(?:/|$)")
    }
}
