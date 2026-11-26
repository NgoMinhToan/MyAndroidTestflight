package dev.minhtoan.mytestflight.data.remote

import dev.minhtoan.mytestflight.domain.settings.ServerSettingsRepository
import javax.inject.Inject
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response

class BaseUrlInterceptor @Inject constructor(
    private val serverSettings: ServerSettingsRepository,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val base = serverSettings.serverUrl.value.toHttpUrlOrNull()
            ?: return chain.proceed(chain.request())

        val request = chain.request()
        val rewritten = rewriteUrl(request.url, base)
        val target = if (rewritten == request.url) {
            request
        } else {
            request.newBuilder().url(rewritten).build()
        }
        return chain.proceed(target)
    }
}

internal fun rewriteUrl(original: HttpUrl, base: HttpUrl): HttpUrl =
    original.newBuilder()
        .scheme(base.scheme)
        .host(base.host)
        .port(base.port)
        .build()
