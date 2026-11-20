package com.mycompany.testflight.data.installer

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.mycompany.testflight.domain.installer.ApkInstaller
import com.mycompany.testflight.domain.installer.InstallFailure
import com.mycompany.testflight.domain.installer.InstallStatus
import com.mycompany.testflight.domain.model.AppVersion
import com.mycompany.testflight.domain.model.TestApp
import com.mycompany.testflight.domain.settings.ServerSettingsRepository
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request

@Singleton
class ApkInstallerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named("downloadClient") private val downloadClient: OkHttpClient,
    private val serverSettings: ServerSettingsRepository,
) : ApkInstaller {

    override fun install(app: TestApp, version: AppVersion): Flow<InstallStatus> = flow {
        if (!context.packageManager.canRequestPackageInstalls()) {
            emit(InstallStatus.PermissionRequired)
            return@flow
        }

        val downloadUrl = serverSettings.serverUrl.value.toHttpUrlOrNull()
            ?.let { base -> version.downloadPath.takeIf { it.isNotBlank() }?.let(base::resolve) }
        if (downloadUrl == null) {
            emit(InstallStatus.Failed(InstallFailure.InvalidServerUrl))
            return@flow
        }

        val cacheDir = File(context.cacheDir, CACHE_DIR_NAME).apply { mkdirs() }
        val target = File(cacheDir, "${sanitize(app.id)}-${sanitize(version.versionName)}.apk")
        val partial = File(cacheDir, "${target.name}.part")
        var launched = false

        try {
            downloadClient.newCall(Request.Builder().url(downloadUrl).build()).execute()
                .use { response ->
                    if (!response.isSuccessful) {
                        emit(InstallStatus.Failed(InstallFailure.Http(response.code)))
                        return@flow
                    }
                    val body = response.body

                    val totalBytes = body.contentLength().takeIf { it > 0 }
                    val digest = MessageDigest.getInstance("SHA-256")
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
                                digest.update(buffer, 0, read)
                                downloadedBytes += read

                                val now = System.currentTimeMillis()
                                val percent =
                                    totalBytes?.let { ((downloadedBytes * 100) / it).toInt() } ?: -1
                                val percentChanged = percent != lastPercent
                                if (firstChunk || percentChanged || now - lastEmitAt >= PROGRESS_INTERVAL_MS) {
                                    firstChunk = false
                                    lastPercent = percent
                                    lastEmitAt = now
                                    emit(InstallStatus.Downloading(downloadedBytes, totalBytes))
                                }
                            }
                        }
                    }

                    emit(InstallStatus.Downloading(downloadedBytes, totalBytes))

                    val expectedSha256 = version.sha256
                    if (expectedSha256 != null) {
                        emit(InstallStatus.Verifying)
                        val actualSha256 = digest.digest().toHex()
                        if (!actualSha256.equals(expectedSha256.lowercase())) {
                            partial.delete()
                            emit(InstallStatus.Failed(InstallFailure.ChecksumMismatch))
                            return@flow
                        }
                    }

                    if (target.exists()) target.delete()
                    if (!partial.renameTo(target)) {
                        throw IOException("Could not move the downloaded APK into place")
                    }

                    emit(InstallStatus.LaunchingInstaller)
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        target,
                    )
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, APK_MIME_TYPE)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    launched = true
                    emit(InstallStatus.InstallerLaunched)
                }
        } catch (error: CancellationException) {
            throw error
        } catch (error: ActivityNotFoundException) {
            emit(InstallStatus.Failed(InstallFailure.NoInstaller))
        } catch (error: SecurityException) {
            emit(InstallStatus.Failed(InstallFailure.NoInstaller))
        } catch (error: IOException) {
            emit(InstallStatus.Failed(InstallFailure.Io(error)))
        } catch (error: Exception) {
            emit(InstallStatus.Failed(InstallFailure.Unexpected(error)))
        } finally {
            if (!launched) partial.delete()
        }
    }.flowOn(Dispatchers.IO)

    private fun sanitize(value: String): String = value.replace(UNSAFE_FILENAME_CHARS, "_")

    private fun ByteArray.toHex(): String = joinToString(separator = "") { byte ->
        "%02x".format(byte.toInt() and 0xFF)
    }

    private companion object {
        const val CACHE_DIR_NAME = "apks"
        const val APK_MIME_TYPE = "application/vnd.android.package-archive"
        const val BUFFER_SIZE = 64 * 1024
        const val PROGRESS_INTERVAL_MS = 100L
        val UNSAFE_FILENAME_CHARS = Regex("[^A-Za-z0-9._-]")
    }
}
