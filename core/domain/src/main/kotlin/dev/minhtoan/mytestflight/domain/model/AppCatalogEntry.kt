package dev.minhtoan.mytestflight.domain.model

import dev.minhtoan.mytestflight.domain.apk.BuildAvailability
import dev.minhtoan.mytestflight.domain.apk.ResolvedBuild
import dev.minhtoan.mytestflight.domain.installer.InstallAction
import dev.minhtoan.mytestflight.domain.installer.InstallActionResolver

/**
 * A catalog row: the app, the build featured for this device (the latest one, or the newest
 * build that actually supports the device), and the action its single button should offer.
 */
data class AppCatalogEntry(
    val app: TestApp,
    val version: AppVersion?,
    val availability: BuildAvailability?,
    val installed: InstalledApp?,
    val action: InstallAction,
) {
    val build: ResolvedBuild?
        get() = version?.let { candidate ->
            (availability as? BuildAvailability.Available)
                ?.let { ResolvedBuild(candidate, it.artifact) }
        }

    fun withInstalled(installed: InstalledApp?): AppCatalogEntry = copy(
        installed = installed,
        action = InstallActionResolver.resolve(version, availability, installed),
    )
}
