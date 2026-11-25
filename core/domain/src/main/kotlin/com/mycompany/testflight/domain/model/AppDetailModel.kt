package com.mycompany.testflight.domain.model

import com.mycompany.testflight.domain.apk.BuildAvailability
import com.mycompany.testflight.domain.apk.ResolvedBuild
import com.mycompany.testflight.domain.installer.InstallAction
import com.mycompany.testflight.domain.installer.InstallActionResolver

data class VersionState(
    val version: AppVersion,
    val availability: BuildAvailability,
) {
    val build: ResolvedBuild?
        get() = (availability as? BuildAvailability.Available)
            ?.let { ResolvedBuild(version, it.artifact) }
}

/**
 * Everything the detail screen needs: the server payload, the featured build for the header
 * action, and per-version availability so cards can disable builds this device cannot use.
 */
data class AppDetailModel(
    val details: AppDetails,
    val installed: InstalledApp?,
    val headerBuild: ResolvedBuild?,
    val actionVersion: AppVersion?,
    val actionAvailability: BuildAvailability?,
    val action: InstallAction,
    val versions: List<VersionState>,
) {
    fun withInstalled(installed: InstalledApp?): AppDetailModel = copy(
        installed = installed,
        action = InstallActionResolver.resolve(actionVersion, actionAvailability, installed),
    )
}
