package com.mycompany.testflight.domain.installer

import com.mycompany.testflight.domain.model.AppVersion
import com.mycompany.testflight.domain.model.TestApp
import kotlinx.coroutines.flow.Flow

sealed interface InstallStatus {
    data class Downloading(val downloadedBytes: Long, val totalBytes: Long?) : InstallStatus
    data object Verifying : InstallStatus
    data object LaunchingInstaller : InstallStatus
    data object InstallerLaunched : InstallStatus
    data object PermissionRequired : InstallStatus
    data class Failed(val message: String) : InstallStatus
}

interface ApkInstaller {
    fun install(app: TestApp, version: AppVersion): Flow<InstallStatus>
}
