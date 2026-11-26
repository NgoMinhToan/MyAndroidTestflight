package dev.minhtoan.mytestflight.domain.apk

import dev.minhtoan.mytestflight.domain.device.DeviceProfile
import dev.minhtoan.mytestflight.domain.model.ApkArtifact
import dev.minhtoan.mytestflight.domain.model.AppVersion

/** Whether a version can be installed on the current device, and with which APK. */
sealed interface BuildAvailability {
    data class Available(val artifact: ApkArtifact) : BuildAvailability
    data object UnsupportedSdk : BuildAvailability
    data object NoCompatibleAbi : BuildAvailability
}

/** A version paired with the concrete APK chosen for this device. */
data class ResolvedBuild(
    val version: AppVersion,
    val artifact: ApkArtifact,
)

object BuildResolver {

    /**
     * Picks the best artifact for [device]: the first device-supported ABI in the device's own
     * preference order, then `universal` as the fallback. A version with no artifacts list is a
     * legacy manifest whose single file is treated as universal.
     */
    fun resolve(version: AppVersion, device: DeviceProfile): BuildAvailability {
        val minSdk = version.minSdk
        if (minSdk != null && device.sdkInt < minSdk) {
            return BuildAvailability.UnsupportedSdk
        }

        if (version.artifacts.isEmpty()) {
            return legacyArtifact(version)
                ?.let(BuildAvailability::Available)
                ?: BuildAvailability.NoCompatibleAbi
        }

        val byAbi = version.artifacts.associateBy { it.abi.lowercase() }
        device.supportedAbis.forEach { abi ->
            byAbi[abi.lowercase()]?.let { return BuildAvailability.Available(it) }
        }
        byAbi[ApkArtifact.UNIVERSAL_ABI]?.let { return BuildAvailability.Available(it) }
        return BuildAvailability.NoCompatibleAbi
    }

    /** Highest versionCode that has an installable artifact for this device, if any. */
    fun bestBuild(versions: List<AppVersion>, device: DeviceProfile): ResolvedBuild? =
        versions.asSequence()
            .sortedByDescending { it.versionCode }
            .mapNotNull { version ->
                (resolve(version, device) as? BuildAvailability.Available)
                    ?.let { ResolvedBuild(version, it.artifact) }
            }
            .firstOrNull()

    private fun legacyArtifact(version: AppVersion): ApkArtifact? =
        version.downloadPath.takeIf { it.isNotBlank() }?.let { path ->
            ApkArtifact(
                abi = ApkArtifact.UNIVERSAL_ABI,
                sizeBytes = version.sizeBytes,
                sha256 = version.sha256,
                downloadPath = path,
            )
        }
}
