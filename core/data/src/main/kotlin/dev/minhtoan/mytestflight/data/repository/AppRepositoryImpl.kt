package dev.minhtoan.mytestflight.data.repository

import dev.minhtoan.mytestflight.data.remote.TestflightApi
import dev.minhtoan.mytestflight.data.remote.dto.AppDto
import dev.minhtoan.mytestflight.data.remote.dto.ArtifactDto
import dev.minhtoan.mytestflight.data.remote.dto.VersionDto
import dev.minhtoan.mytestflight.domain.error.AppError
import dev.minhtoan.mytestflight.domain.model.ApkArtifact
import dev.minhtoan.mytestflight.domain.model.AppDetails
import dev.minhtoan.mytestflight.domain.model.AppVersion
import dev.minhtoan.mytestflight.domain.model.TestApp
import dev.minhtoan.mytestflight.domain.repository.AppRepository
import dev.minhtoan.mytestflight.domain.settings.ServerSettingsRepository
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
        artifacts = artifacts.mapNotNull { it.toDomain() },
    )

    private fun ArtifactDto.toDomain(): ApkArtifact? {
        if (abi.isBlank() || downloadPath.isBlank()) return null
        return ApkArtifact(
            abi = abi,
            sizeBytes = sizeBytes,
            sha256 = sha256,
            downloadPath = downloadPath,
        )
    }
}
