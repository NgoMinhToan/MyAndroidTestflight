package dev.minhtoan.mytestflight.domain.installer

import dev.minhtoan.mytestflight.domain.apk.BuildAvailability
import dev.minhtoan.mytestflight.domain.model.ApkArtifact
import dev.minhtoan.mytestflight.domain.model.AppVersion
import dev.minhtoan.mytestflight.domain.model.InstalledApp
import org.junit.Assert.assertEquals
import org.junit.Test

class InstallActionResolverTest {

    private val artifact = ApkArtifact(
        abi = "arm64-v8a",
        sizeBytes = 1_024L,
        sha256 = null,
        downloadPath = "/api/v1/apps/demo/versions/1.0.0/download?abi=arm64-v8a",
    )
    private val available = BuildAvailability.Available(artifact)
    private val version = AppVersion(
        versionName = "1.0.0",
        versionCode = 10,
        minSdk = 26,
        releaseNotes = null,
        publishedAt = "2026-11-19T08:00:00.000Z",
        sizeBytes = 1_024L,
        sha256 = null,
        downloadPath = "/api/v1/apps/demo/versions/1.0.0/download",
    )

    @Test
    fun `not installed with a compatible build offers install`() {
        assertEquals(
            InstallAction.Install,
            InstallActionResolver.resolve(version, available, installed = null),
        )
    }

    @Test
    fun `not installed without a compatible build is unsupported`() {
        assertEquals(
            InstallAction.Unsupported,
            InstallActionResolver.resolve(
                version,
                BuildAvailability.NoCompatibleAbi,
                installed = null,
            ),
        )
    }

    @Test
    fun `installed behind the server offers update`() {
        assertEquals(
            InstallAction.Update,
            InstallActionResolver.resolve(version, available, installed(versionCode = 9)),
        )
    }

    @Test
    fun `installed at the same version offers open`() {
        assertEquals(
            InstallAction.Open,
            InstallActionResolver.resolve(version, available, installed(versionCode = 10)),
        )
    }

    @Test
    fun `installed ahead of the server offers open`() {
        assertEquals(
            InstallAction.Open,
            InstallActionResolver.resolve(version, available, installed(versionCode = 11)),
        )
    }

    @Test
    fun `installed without a compatible build still offers open`() {
        assertEquals(
            InstallAction.Open,
            InstallActionResolver.resolve(
                version,
                BuildAvailability.UnsupportedSdk,
                installed(versionCode = 1),
            ),
        )
    }

    @Test
    fun `installed with no server version still offers open`() {
        assertEquals(
            InstallAction.Open,
            InstallActionResolver.resolve(null, null, installed(versionCode = 1)),
        )
    }

    private fun installed(versionCode: Long) =
        InstalledApp(packageName = "com.demo", versionCode = versionCode)
}
