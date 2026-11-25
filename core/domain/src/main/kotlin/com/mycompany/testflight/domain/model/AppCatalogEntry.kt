package com.mycompany.testflight.domain.model

import com.mycompany.testflight.domain.apk.BuildAvailability
import com.mycompany.testflight.domain.apk.ResolvedBuild
import com.mycompany.testflight.domain.installer.InstallAction
import com.mycompany.testflight.domain.installer.InstallActionResolver

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
