package com.mycompany.testflight.data.repository

import com.mycompany.testflight.data.remote.TestflightApi
import com.mycompany.testflight.data.remote.dto.AppDto
import com.mycompany.testflight.data.remote.dto.AppResponseDto
import com.mycompany.testflight.data.remote.dto.AppsResponseDto
import com.mycompany.testflight.data.remote.dto.ArtifactDto
import com.mycompany.testflight.data.remote.dto.VersionDto
import com.mycompany.testflight.domain.error.AppError
import com.mycompany.testflight.domain.settings.ServerSettingsRepository
import com.mycompany.testflight.domain.settings.ServerUrlValidation
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class AppRepositoryImplTest {

    private val versionDto = VersionDto(
        versionName = "1.0.0",
        versionCode = 1,
        minSdk = 26,
        releaseNotes = "First build",
        publishedAt = "2026-11-19T08:00:00.000Z",
        sizeBytes = 1_024L,
        sha256 = null,
        downloadPath = "/api/v1/apps/demo/versions/1.0.0/download",
    )

    private val appDto = AppDto(
        id = "demo",
        name = "Demo App",
        packageName = "com.demo",
        description = "Internal build",
        developer = "Platform Team",
        versionCount = 1,
        iconPath = "/api/v1/apps/demo/icon",
        latestVersion = versionDto,
        versions = listOf(versionDto),
    )

    @Test
    fun `maps apps and resolves the icon against the current base url`() = runTest {
        val api = FakeApi(appsResponse = AppsResponseDto(listOf(appDto)))
        val settings = FakeServerSettings("http://127.0.0.1:8080")
        val repository = AppRepositoryImpl(api, settings)

        val apps = repository.getApps().getOrThrow()

        val app = apps.single()
        assertEquals("Demo App", app.name)
        assertEquals("Internal build", app.description)
        assertEquals("Platform Team", app.developer)
        assertEquals(1, app.versionCount)
        assertEquals("http://127.0.0.1:8080/api/v1/apps/demo/icon", app.iconUrl)
        assertEquals(
            "/api/v1/apps/demo/versions/1.0.0/download",
            app.latestVersion?.downloadPath,
        )
    }

    @Test
    fun `uses the latest server url for icon resolution`() = runTest {
        val api = FakeApi(appsResponse = AppsResponseDto(listOf(appDto)))
        val settings = FakeServerSettings("http://127.0.0.1:8080")
        val repository = AppRepositoryImpl(api, settings)

        settings.serverUrl.value = "https://example.com:9443"

        val app = repository.getApps().getOrThrow().single()
        assertEquals("https://example.com:9443/api/v1/apps/demo/icon", app.iconUrl)
    }

    @Test
    fun `maps app details with every version`() = runTest {
        val secondVersion = versionDto.copy(versionName = "0.9.0", versionCode = 9)
        val api = FakeApi(
            appsResponse = AppsResponseDto(listOf(appDto)),
            appResponse = AppResponseDto(
                appDto.copy(versions = listOf(versionDto, secondVersion)),
            ),
        )
        val repository = AppRepositoryImpl(api, FakeServerSettings("http://127.0.0.1:8080"))

        val details = repository.getApp("demo").getOrThrow()

        assertEquals("demo", details.app.id)
        assertEquals(listOf("1.0.0", "0.9.0"), details.versions.map { it.versionName })
    }

    @Test
    fun `maps per-abi artifacts and drops unusable entries`() = runTest {
        val withArtifacts = versionDto.copy(
            artifacts = listOf(
                ArtifactDto(
                    abi = "arm64-v8a",
                    sizeBytes = 2_048L,
                    sha256 = "abc",
                    downloadPath = "/api/v1/apps/demo/versions/1.0.0/download?abi=arm64-v8a",
                ),
                ArtifactDto(
                    abi = "universal",
                    sizeBytes = 4_096L,
                    sha256 = null,
                    downloadPath = "/api/v1/apps/demo/versions/1.0.0/download?abi=universal",
                ),
                ArtifactDto(abi = "", downloadPath = "/ignored.apk"),
                ArtifactDto(abi = "x86_64", downloadPath = ""),
            ),
        )
        val api = FakeApi(
            appsResponse = AppsResponseDto(
                listOf(appDto.copy(latestVersion = withArtifacts)),
            ),
        )
        val repository = AppRepositoryImpl(api, FakeServerSettings("http://127.0.0.1:8080"))

        val resolved = repository.getApps().getOrThrow().single().latestVersion

        assertEquals(listOf("arm64-v8a", "universal"), resolved?.artifacts?.map { it.abi })
        assertEquals(2_048L, resolved?.artifacts?.first()?.sizeBytes)
    }

    @Test
    fun `maps a 404 to a server error`() = runTest {
        val response = Response.error<Any>(
            404,
            """{"error":{"code":"app_not_found"}}""".toResponseBody("application/json".toMediaType()),
        )
        val api = FakeApi(appError = HttpException(response))
        val repository = AppRepositoryImpl(api, FakeServerSettings("http://127.0.0.1:8080"))

        val error = repository.getApp("missing").exceptionOrNull()

        assertTrue(error is AppError.Server)
        assertEquals(404, (error as AppError.Server).code)
    }

    @Test
    fun `maps io failures to a network error`() = runTest {
        val api = FakeApi(error = IOException("offline"))
        val repository = AppRepositoryImpl(api, FakeServerSettings("http://127.0.0.1:8080"))

        val error = repository.getApps().exceptionOrNull()

        assertTrue(error is AppError.Network)
    }

    @Test
    fun `fails with invalid server url when the base cannot be parsed`() = runTest {
        val repository = AppRepositoryImpl(FakeApi(), FakeServerSettings("not a url"))

        val error = repository.getApps().exceptionOrNull()

        assertTrue(error is AppError.InvalidServerUrl)
    }

    @Test
    fun `rethrows cancellation`() = runTest {
        val api = FakeApi(error = CancellationException("cancelled"))
        val repository = AppRepositoryImpl(api, FakeServerSettings("http://127.0.0.1:8080"))

        val thrown = runCatching { repository.getApps() }.exceptionOrNull()

        assertTrue(thrown is CancellationException)
    }

    private class FakeApi(
        private val appsResponse: AppsResponseDto = AppsResponseDto(),
        private val appResponse: AppResponseDto? = null,
        private val error: Throwable? = null,
        private val appError: Throwable? = null,
    ) : TestflightApi {
        override suspend fun getApps(): AppsResponseDto {
            error?.let { throw it }
            return appsResponse
        }

        override suspend fun getApp(appId: String): AppResponseDto {
            appError?.let { throw it }
            error?.let { throw it }
            return checkNotNull(appResponse)
        }
    }

    private class FakeServerSettings(initial: String) : ServerSettingsRepository {
        override val serverUrl: MutableStateFlow<String> = MutableStateFlow(initial)
        override val defaultServerUrl: String = initial

        override suspend fun updateServerUrl(raw: String): ServerUrlValidation {
            serverUrl.value = raw
            return ServerUrlValidation.Valid(raw)
        }

        override suspend fun resetToDefault() {
            serverUrl.value = defaultServerUrl
        }
    }
}
