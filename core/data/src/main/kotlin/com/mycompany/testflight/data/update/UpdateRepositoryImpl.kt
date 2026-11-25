package com.mycompany.testflight.data.update

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.FileProvider
import com.mycompany.testflight.domain.update.AppInfoProvider
import com.mycompany.testflight.domain.update.AppRelease
import com.mycompany.testflight.domain.update.AssetSelector
import com.mycompany.testflight.domain.update.DownloadedApk
import com.mycompany.testflight.domain.update.InstallLaunch
import com.mycompany.testflight.domain.update.ReleaseAsset
import com.mycompany.testflight.domain.update.UpdateCheck
import com.mycompany.testflight.domain.update.UpdateDownload
import com.mycompany.testflight.domain.update.UpdateError
import com.mycompany.testflight.domain.update.UpdateException
import com.mycompany.testflight.domain.update.UpdateRepository
import com.mycompany.testflight.domain.update.VersionComparator
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.HttpException

/**
 * Self-update backed by the public GitHub repository. `releases/latest` already skips drafts and
 * pre-releases; they are filtered again so a hand-edited release cannot slip through.
 */
@Singleton
class UpdateRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: GitHubReleaseApi,
    @Named("downloadClient") private val downloadClient: OkHttpClient,
    private val appInfo: AppInfoProvider,
) : UpdateRepository {

    override suspend fun checkForUpdate(): Result<UpdateCheck> {
        val current = appInfo.version
        return try {
            val dto = withRetry { api.getLatestRelease(OWNER, REPO) }
            val release = if (dto.draft || dto.prerelease) null else dto.toRelease()
            val newer = release?.let { isNewer(it, current.versionCode, current.versionName) } == true
            Result.success(UpdateCheck(current = current, release = release.takeIf { newer }))
        } catch (error: CancellationException) {
            throw error
        } catch (error: HttpException) {
            val mapped = if (error.code() == HTTP_NOT_FOUND) {
                UpdateError.NoStableRelease
            } else {
                UpdateError.Server
            }
            Result.failure(UpdateException(mapped, error))
        } catch (error: IOException) {
            Result.failure(UpdateException(UpdateError.Network, error))
        } catch (error: Exception) {
            Result.failure(UpdateException(UpdateError.Unexpected, error))
        }
    }

    override fun download(release: AppRelease): Flow<UpdateDownload> = flow {
        val asset = AssetSelector.select(release.assets, Build.SUPPORTED_ABIS.toList())
            ?: throw UpdateException(UpdateError.NoCompatibleAsset)

        val directory = File(context.filesDir, UPDATE_DIR_NAME).apply { mkdirs() }
        if (!directory.isDirectory) throw UpdateException(UpdateError.Storage)
        pruneOtherApks(directory, keep = null)

        val target = File(directory, sanitizeAssetName(asset.name))
        val partial = File(directory, "${target.name}.part")
        var attempt = 0

        try {
            while (true) {
                attempt++
                try {
                    downloadToFile(asset, partial) { downloaded, total ->
                        emit(UpdateDownload.Progress(downloaded, total))
                    }
                    break
                } catch (error: IOException) {
                    partial.delete()
                    if (attempt >= MAX_ATTEMPTS) throw error
                    delay(RETRY_DELAY_MS * attempt)
                }
            }

            val expected = asset.sha256?.lowercase()
            if (!expected.isNullOrBlank()) {
                emit(UpdateDownload.Verifying)
                val actual = partial.sha256()
                if (!actual.equals(expected, ignoreCase = true)) {
                    partial.delete()
                    throw UpdateException(UpdateError.ChecksumMismatch)
                }
            }

            if (target.exists() && !target.delete()) {
                throw UpdateException(UpdateError.Storage)
            }
            if (!partial.renameTo(target)) {
                throw UpdateException(UpdateError.Storage)
            }

            pruneOtherApks(directory, keep = target)
            emit(
                UpdateDownload.Completed(
                    DownloadedApk(
                        versionName = release.versionName,
                        path = target.absolutePath,
                        sha256 = expected,
                    ),
                ),
            )
        } catch (error: CancellationException) {
            partial.delete()
            throw error
        } catch (error: UpdateException) {
            partial.delete()
            throw error
        } catch (error: IOException) {
            partial.delete()
            throw UpdateException(UpdateError.Network, error)
        } catch (error: Exception) {
            partial.delete()
            throw UpdateException(UpdateError.Unexpected, error)
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun install(apk: DownloadedApk): InstallLaunch {
        if (!context.packageManager.canRequestPackageInstalls()) {
            return InstallLaunch.PermissionRequired
        }
        val file = File(apk.path)
        if (!file.isFile) return InstallLaunch.Failed(UpdateError.Storage)

        return try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file,
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, APK_MIME_TYPE)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            InstallLaunch.Launched
        } catch (error: ActivityNotFoundException) {
            InstallLaunch.Failed(UpdateError.NoInstaller)
        } catch (error: SecurityException) {
            InstallLaunch.Failed(UpdateError.NoInstaller)
        } catch (error: Exception) {
            InstallLaunch.Failed(UpdateError.Unexpected)
        }
    }

    private suspend fun downloadToFile(
        asset: ReleaseAsset,
        partial: File,
        onProgress: suspend (downloadedBytes: Long, totalBytes: Long?) -> Unit,
    ) {
        downloadClient.newCall(Request.Builder().url(asset.url).build()).execute().use { response ->
            if (!response.isSuccessful) {
                if (response.code in SERVER_ERROR_RANGE) {
                    throw IOException("GitHub download failed with HTTP ${response.code}")
                }
                throw UpdateException(UpdateError.Server)
            }

            val body = response.body
            val totalBytes = body.contentLength().takeIf { it > 0 }
                ?: asset.sizeBytes.takeIf { it > 0 }

            partial.delete()
            var downloadedBytes = 0L
            var firstChunk = true
            var lastPercent = -1
            var lastEmitAt = 0L

            body.byteStream().use { input ->
                FileOutputStream(partial).use { output ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        if (read == 0) continue

                        output.write(buffer, 0, read)
                        downloadedBytes += read

                        val now = System.currentTimeMillis()
                        val percent = totalBytes?.let {
                            ((downloadedBytes * 100) / it).toInt()
                        } ?: -1
                        if (firstChunk ||
                            percent != lastPercent ||
                            now - lastEmitAt >= PROGRESS_INTERVAL_MS
                        ) {
                            firstChunk = false
                            lastPercent = percent
                            lastEmitAt = now
                            onProgress(downloadedBytes, totalBytes)
                        }
                    }
                }
            }
            onProgress(downloadedBytes, totalBytes)
        }
    }

    private suspend fun <T> withRetry(block: suspend () -> T): T {
        var attempt = 0
        while (true) {
            attempt++
            try {
                return block()
            } catch (error: CancellationException) {
                throw error
            } catch (error: IOException) {
                if (attempt >= MAX_ATTEMPTS) throw error
                delay(RETRY_DELAY_MS * attempt)
            }
        }
    }

    private fun GitHubReleaseDto.toRelease(): AppRelease? {
        val parsed = ReleaseBodyParser.parse(body)
        val tagVersion = tagName.removePrefix("v").ifBlank { name.orEmpty() }
        val versionName = parsed.version ?: tagVersion.ifBlank { return null }
        val assets = parsed.assets.ifEmpty { GitHubReleaseMapper.assetsFromApi(assets) }
        return AppRelease(
            versionName = versionName,
            versionCode = parsed.versionCode,
            tag = parsed.tag ?: tagName.ifBlank { "v$versionName" },
            publishedAt = parsed.publishedAt ?: publishedAt,
            notes = parsed.notes,
            assets = assets,
            releaseUrl = htmlUrl,
        )
    }

    private fun isNewer(release: AppRelease, currentCode: Long, currentName: String): Boolean {
        val releaseCode = release.versionCode
        if (releaseCode != null && currentCode > 0) {
            if (releaseCode.toLong() != currentCode) return releaseCode.toLong() > currentCode
        }
        return VersionComparator.isNewer(release.versionName, currentName)
    }

    private fun pruneOtherApks(directory: File, keep: File?) {
        directory.listFiles()?.forEach { file ->
            if (file != keep && file.name != "${keep?.name}.part") {
                file.delete()
            }
        }
    }

    private fun sanitizeAssetName(name: String): String =
        name.replace(UNSAFE_FILENAME_CHARS, "_").ifBlank { "update.apk" }

    private fun File.sha256(): String {
        val digest = MessageDigest.getInstance("SHA-256")
        inputStream().use { input ->
            val buffer = ByteArray(BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString(separator = "") { "%02x".format(it.toInt() and 0xFF) }
    }

    private companion object {
        const val OWNER = "NgoMinhToan"
        const val REPO = "MyAndroidTestflight"
        const val UPDATE_DIR_NAME = "updates"
        const val APK_MIME_TYPE = "application/vnd.android.package-archive"
        const val BUFFER_SIZE = 64 * 1024
        const val PROGRESS_INTERVAL_MS = 150L
        const val MAX_ATTEMPTS = 2
        const val RETRY_DELAY_MS = 700L
        const val HTTP_NOT_FOUND = 404
        val SERVER_ERROR_RANGE = 500..599
        val UNSAFE_FILENAME_CHARS = Regex("[^A-Za-z0-9._-]")
    }
}
