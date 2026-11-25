package com.mycompany.testflight.domain.repository

import com.mycompany.testflight.domain.model.InstalledApp

interface InstalledAppRepository {
    /** Looks up the installed copy of each package. Missing packages are omitted. */
    suspend fun installedApps(packageNames: Collection<String>): Map<String, InstalledApp>
}
