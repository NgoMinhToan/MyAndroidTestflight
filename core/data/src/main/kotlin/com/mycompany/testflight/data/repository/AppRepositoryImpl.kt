package com.mycompany.testflight.data.repository

import com.mycompany.testflight.data.remote.TestflightApi
import com.mycompany.testflight.data.remote.dto.AppDto
import com.mycompany.testflight.domain.error.AppError
import com.mycompany.testflight.domain.model.AppVersion
import com.mycompany.testflight.domain.model.TestApp
import com.mycompany.testflight.domain.repository.AppRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import retrofit2.HttpException

@Singleton
class AppRepositoryImpl @Inject constructor(
    private val api: TestflightApi,
    @Named("baseUrl") baseUrl: String,
) : AppRepository {

    private val base: HttpUrl? = baseUrl.toHttpUrlOrNull()

    override suspend fun getApps(): Result<List<TestApp>> = try {
        Result.success(api.getApps().apps.map { it.toDomain(base) })
    } catch (error: CancellationException) {
        throw error
    } catch (error: IOException) {
        Result.failure(AppError.Network("Can't reach the server", error))
    } catch (error: HttpException) {
        Result.failure(AppError.Server("Server returned HTTP ${error.code()}"))
    } catch (error: Exception) {
        Result.failure(AppError.Unknown(error.message ?: "Unexpected error", error))
    }

    private fun AppDto.toDomain(base: HttpUrl?): TestApp {
        val resolvedIconUrl = iconPath?.let { base?.resolve(it)?.toString() }
        val resolvedVersion = latestVersion?.let { dto ->
            val downloadUrl = base?.resolve(dto.downloadPath)?.toString() ?: return@let null
            AppVersion(
                versionName = dto.versionName,
                versionCode = dto.versionCode,
                minSdk = dto.minSdk,
                releaseNotes = dto.releaseNotes,
                publishedAt = dto.publishedAt,
                sizeBytes = dto.sizeBytes,
                sha256 = dto.sha256,
                downloadUrl = downloadUrl,
            )
        }
        return TestApp(
            id = id,
            name = name.ifBlank { id },
            packageName = packageName,
            iconUrl = resolvedIconUrl,
            latestVersion = resolvedVersion,
        )
    }
}
