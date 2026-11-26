package dev.minhtoan.mytestflight.domain.repository

import dev.minhtoan.mytestflight.domain.model.InstalledApp

interface InstalledAppRepository {
    /** Looks up the installed copy of each package. Missing packages are omitted. */
    suspend fun installedApps(packageNames: Collection<String>): Map<String, InstalledApp>
}
