package dev.minhtoan.mytestflight.domain.installer

import dev.minhtoan.mytestflight.domain.apk.ResolvedBuild
import dev.minhtoan.mytestflight.domain.model.TestApp
import kotlinx.coroutines.flow.Flow

sealed interface InstallFailure {
    data class Http(val code: Int) : InstallFailure
    data object EmptyBody : InstallFailure
    data object ChecksumMismatch : InstallFailure
    data object NoInstaller : InstallFailure
    data object InvalidServerUrl : InstallFailure
    data class Io(val cause: Throwable? = null) : InstallFailure
    data class Unexpected(val cause: Throwable? = null) : InstallFailure
}

sealed interface InstallStatus {
    data class Downloading(val downloadedBytes: Long, val totalBytes: Long?) : InstallStatus
    data object Verifying : InstallStatus
    data object LaunchingInstaller : InstallStatus
    data object InstallerLaunched : InstallStatus
    data object PermissionRequired : InstallStatus
    data class Failed(val failure: InstallFailure) : InstallStatus
}

interface ApkInstaller {
    fun install(app: TestApp, build: ResolvedBuild): Flow<InstallStatus>
}
