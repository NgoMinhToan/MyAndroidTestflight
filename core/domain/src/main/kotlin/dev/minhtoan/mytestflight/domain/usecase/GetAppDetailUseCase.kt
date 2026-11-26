package dev.minhtoan.mytestflight.domain.usecase

import dev.minhtoan.mytestflight.domain.apk.BuildAvailability
import dev.minhtoan.mytestflight.domain.apk.BuildResolver
import dev.minhtoan.mytestflight.domain.device.DeviceProfileProvider
import dev.minhtoan.mytestflight.domain.installer.InstallActionResolver
import dev.minhtoan.mytestflight.domain.model.AppDetailModel
import dev.minhtoan.mytestflight.domain.model.VersionState
import dev.minhtoan.mytestflight.domain.repository.AppRepository
import dev.minhtoan.mytestflight.domain.repository.InstalledAppRepository
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

class GetAppDetailUseCase @Inject constructor(
    private val repository: AppRepository,
    private val installedRepository: InstalledAppRepository,
    private val deviceProfileProvider: DeviceProfileProvider,
) {
    suspend operator fun invoke(id: String): Result<AppDetailModel> = try {
        val details = repository.getApp(id).getOrThrow()
        val device = deviceProfileProvider.current()
        val packageName = details.app.packageName
        val installed = installedRepository
            .installedApps(listOf(packageName))[packageName]

        val versions = details.versions.map { version ->
            VersionState(version, BuildResolver.resolve(version, device))
        }
        val headerBuild = BuildResolver.bestBuild(details.versions, device)
        val displayVersion = details.app.latestVersion
            ?: details.versions.maxByOrNull { it.versionCode }
        val actionVersion = headerBuild?.version ?: displayVersion
        val actionAvailability = headerBuild
            ?.let { BuildAvailability.Available(it.artifact) }
            ?: displayVersion?.let { BuildResolver.resolve(it, device) }

        Result.success(
            AppDetailModel(
                details = details,
                installed = installed,
                headerBuild = headerBuild,
                actionVersion = actionVersion,
                actionAvailability = actionAvailability,
                action = InstallActionResolver.resolve(
                    actionVersion,
                    actionAvailability,
                    installed,
                ),
                versions = versions,
            ),
        )
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        Result.failure(error)
    }
}
