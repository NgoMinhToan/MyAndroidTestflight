package dev.minhtoan.mytestflight.domain.update

import kotlinx.coroutines.flow.Flow

sealed interface InstallLaunch {
    data object Launched : InstallLaunch

    data object PermissionRequired : InstallLaunch

    data class Failed(val error: UpdateError) : InstallLaunch
}

interface UpdateRepository {
    /** Looks up the newest stable release and compares it with the installed version. */
    suspend fun checkForUpdate(): Result<UpdateCheck>

    /** Streams the APK for [release] that fits this device, verifying its SHA-256 when known. */
    fun download(release: AppRelease): Flow<UpdateDownload>

    /** Opens the system installer for an already downloaded APK. */
    suspend fun install(apk: DownloadedApk): InstallLaunch
}
