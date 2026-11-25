package com.mycompany.testflight.domain.usecase

import com.mycompany.testflight.domain.apk.BuildAvailability
import com.mycompany.testflight.domain.apk.BuildResolver
import com.mycompany.testflight.domain.device.DeviceProfile
import com.mycompany.testflight.domain.device.DeviceProfileProvider
import com.mycompany.testflight.domain.installer.InstallActionResolver
import com.mycompany.testflight.domain.model.AppCatalogEntry
import com.mycompany.testflight.domain.model.AppVersion
import com.mycompany.testflight.domain.model.InstalledApp
import com.mycompany.testflight.domain.model.TestApp
import com.mycompany.testflight.domain.repository.AppRepository
import com.mycompany.testflight.domain.repository.InstalledAppRepository
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * Loads the catalog and resolves it for this device: the featured build falls back to the newest
 * version that supports the device when the server's latest build does not, and the row action
 * reflects whether the package is installed and behind the server.
 */
class GetCatalogUseCase @Inject constructor(
    private val repository: AppRepository,
    private val installedRepository: InstalledAppRepository,
    private val deviceProfileProvider: DeviceProfileProvider,
) {
    suspend operator fun invoke(): Result<List<AppCatalogEntry>> = try {
        Result.success(load())
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        Result.failure(error)
    }

    private suspend fun load(): List<AppCatalogEntry> {
        val apps = repository.getApps().getOrThrow()
        val device = deviceProfileProvider.current()
        val installed = installedRepository.installedApps(apps.map { it.packageName })
        return coroutineScope {
            apps.map { app ->
                async { resolveEntry(app, device, installed[app.packageName]) }
            }.awaitAll()
        }
    }

    private suspend fun resolveEntry(
        app: TestApp,
        device: DeviceProfile,
        installed: InstalledApp?,
    ): AppCatalogEntry {
        val latest = app.latestVersion
        val latestAvailability = latest?.let { BuildResolver.resolve(it, device) }
        if (latest == null || latestAvailability is BuildAvailability.Available) {
            return entry(app, latest, latestAvailability, installed)
        }

        val fallback = repository.getApp(app.id).getOrNull()
            ?.let { BuildResolver.bestBuild(it.versions, device) }
        return if (fallback != null) {
            entry(
                app = app,
                version = fallback.version,
                availability = BuildAvailability.Available(fallback.artifact),
                installed = installed,
            )
        } else {
            entry(app, latest, latestAvailability, installed)
        }
    }

    private fun entry(
        app: TestApp,
        version: AppVersion?,
        availability: BuildAvailability?,
        installed: InstalledApp?,
    ): AppCatalogEntry = AppCatalogEntry(
        app = app,
        version = version,
        availability = availability,
        installed = installed,
        action = InstallActionResolver.resolve(version, availability, installed),
    )
}
