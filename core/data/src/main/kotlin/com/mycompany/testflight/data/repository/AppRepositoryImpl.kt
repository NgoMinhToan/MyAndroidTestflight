package com.mycompany.testflight.data.repository

import com.mycompany.testflight.data.remote.TestflightApi
import com.mycompany.testflight.data.remote.dto.AppDto
import com.mycompany.testflight.data.remote.dto.VersionDto
import com.mycompany.testflight.domain.error.AppError
import com.mycompany.testflight.domain.model.AppDetails
import com.mycompany.testflight.domain.model.AppVersion
import com.mycompany.testflight.domain.model.TestApp
import com.mycompany.testflight.domain.repository.AppRepository
import com.mycompany.testflight.domain.settings.ServerSettingsRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import retrofit2.HttpException

@Singleton
class AppRepositoryImpl @Inject constructor(
    private val api: TestflightApi,
    private val serverSettings: ServerSettingsRepository,
) : AppRepository {

    override suspend fun getApps(): Result<List<TestApp>> = load { base ->
        api.getApps().apps.map { it.toDomain(base) }
    }

    override suspend fun getApp(id: String): Result<AppDetails> = load { base ->
        val app = api.getApp(id).app
        AppDetails(
            app = app.toDomain(base),
            versions = app.versions.map { it.toDomain() },
        )
    }

    private suspend fun <T> load(block: suspend (HttpUrl) -> T): Result<T> {
        val base = serverSettings.serverUrl.value.toHttpUrlOrNull()
            ?: return Result.failure(AppError.InvalidServerUrl())
        return try {
            Result.success(block(base))
        } catch (error: CancellationException) {
            throw error
        } catch (error: IOException) {
            Result.failure(AppError.Network(error))
        } catch (error: HttpException) {
            Result.failure(AppError.Server(error.code()))
        } catch (error: Exception) {
            Result.failure(AppError.Unexpected(error))
        }
    }

    private fun AppDto.toDomain(base: HttpUrl): TestApp = TestApp(
        id = id,
        name = name.ifBlank { id },
        packageName = packageName,
        iconUrl = iconPath?.let { base.resolve(it)?.toString() },
        latestVersion = latestVersion?.toDomain(),
        description = description,
        developer = developer,
        versionCount = versionCount,
    )

    private fun VersionDto.toDomain(): AppVersion = AppVersion(
        versionName = versionName,
        versionCode = versionCode,
        minSdk = minSdk,
        releaseNotes = releaseNotes,
        publishedAt = publishedAt,
        sizeBytes = sizeBytes,
        sha256 = sha256,
        downloadPath = downloadPath,
    )
}
