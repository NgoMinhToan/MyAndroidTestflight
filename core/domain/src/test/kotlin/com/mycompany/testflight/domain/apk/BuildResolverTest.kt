package com.mycompany.testflight.domain.apk

import com.mycompany.testflight.domain.device.DeviceProfile
import com.mycompany.testflight.domain.model.ApkArtifact
import com.mycompany.testflight.domain.model.AppVersion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildResolverTest {

    private val device = DeviceProfile(
        supportedAbis = listOf("arm64-v8a", "armeabi-v7a"),
        sdkInt = 34,
    )

    @Test
    fun `prefers a device abi over universal`() {
        val version = version(
            artifacts = listOf(
                artifact("universal"),
                artifact("arm64-v8a"),
                artifact("armeabi-v7a"),
            ),
        )

        val availability = BuildResolver.resolve(version, device) as BuildAvailability.Available

        assertEquals("arm64-v8a", availability.artifact.abi)
    }

    @Test
    fun `follows the device abi preference order`() {
        val device = device.copy(supportedAbis = listOf("armeabi-v7a", "arm64-v8a"))
        val version = version(
            artifacts = listOf(artifact("arm64-v8a"), artifact("armeabi-v7a")),
        )

        val availability = BuildResolver.resolve(version, device) as BuildAvailability.Available

        assertEquals("armeabi-v7a", availability.artifact.abi)
    }

    @Test
    fun `falls back to universal when no device abi matches`() {
        val version = version(
            artifacts = listOf(artifact("x86_64"), artifact("universal")),
        )

        val availability = BuildResolver.resolve(version, device) as BuildAvailability.Available

        assertEquals("universal", availability.artifact.abi)
    }

    @Test
    fun `reports no compatible abi when neither the device nor universal is published`() {
        val version = version(artifacts = listOf(artifact("x86_64")))

        assertEquals(
            BuildAvailability.NoCompatibleAbi,
            BuildResolver.resolve(version, device),
        )
    }

    @Test
    fun `rejects a version whose min sdk is above the device`() {
        val version = version(minSdk = 35, artifacts = listOf(artifact("arm64-v8a")))

        assertEquals(
            BuildAvailability.UnsupportedSdk,
            BuildResolver.resolve(version, device),
        )
    }

    @Test
    fun `treats a legacy version without artifacts as universal`() {
        val version = version(artifacts = emptyList())

        val availability = BuildResolver.resolve(version, device) as BuildAvailability.Available

        assertEquals("universal", availability.artifact.abi)
        assertEquals(version.downloadPath, availability.artifact.downloadPath)
        assertEquals(version.sha256, availability.artifact.sha256)
    }

    @Test
    fun `best build skips versions this device cannot install`() {
        val incompatible = version(
            versionName = "2.0.0",
            versionCode = 20,
            artifacts = listOf(artifact("x86_64")),
        )
        val compatible = version(
            versionName = "1.5.0",
            versionCode = 15,
            artifacts = listOf(artifact("arm64-v8a")),
        )

        val best = BuildResolver.bestBuild(listOf(incompatible, compatible), device)

        assertEquals("1.5.0", best?.version?.versionName)
        assertEquals("arm64-v8a", best?.artifact?.abi)
    }

    @Test
    fun `best build is null when every version is incompatible`() {
        val version = version(artifacts = listOf(artifact("x86_64")))

        assertTrue(BuildResolver.bestBuild(listOf(version), device) == null)
    }

    private fun version(
        versionName: String = "1.0.0",
        versionCode: Int = 1,
        minSdk: Int? = 26,
        artifacts: List<ApkArtifact>,
    ) = AppVersion(
        versionName = versionName,
        versionCode = versionCode,
        minSdk = minSdk,
        releaseNotes = null,
        publishedAt = "2026-11-19T08:00:00.000Z",
        sizeBytes = 1_024L,
        sha256 = "abc",
        downloadPath = "/api/v1/apps/demo/versions/$versionName/download",
        artifacts = artifacts,
    )

    private fun artifact(abi: String) = ApkArtifact(
        abi = abi,
        sizeBytes = 1_024L,
        sha256 = null,
        downloadPath = "/api/v1/apps/demo/versions/1.0.0/download?abi=$abi",
    )
}
