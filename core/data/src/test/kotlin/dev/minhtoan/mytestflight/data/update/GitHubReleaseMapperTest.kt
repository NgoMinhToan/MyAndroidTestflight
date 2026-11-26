package dev.minhtoan.mytestflight.data.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GitHubReleaseMapperTest {

    @Test
    fun `infers multi part abis from asset names`() {
        assertEquals(
            "arm64-v8a",
            GitHubReleaseMapper.abiFromName("MyTestflight-1.4.0-arm64-v8a.apk"),
        )
        assertEquals(
            "armeabi-v7a",
            GitHubReleaseMapper.abiFromName("MyTestflight-1.4.0-armeabi-v7a.apk"),
        )
        assertEquals(
            "x86_64",
            GitHubReleaseMapper.abiFromName("MyTestflight-1.4.0-x86_64.apk"),
        )
        assertEquals(
            "universal",
            GitHubReleaseMapper.abiFromName("MyTestflight-1.4.0-universal.apk"),
        )
    }

    @Test
    fun `ignores non apk files`() {
        assertNull(GitHubReleaseMapper.abiFromName("MyTestflight-1.4.0.aab"))
        assertNull(GitHubReleaseMapper.abiFromName("SHA-256SUMS.txt"))
    }

    @Test
    fun `maps api assets and strips the sha256 prefix`() {
        val assets = GitHubReleaseMapper.assetsFromApi(
            listOf(
                GitHubAssetDto(
                    name = "MyTestflight-1.4.0-arm64-v8a.apk",
                    browserDownloadUrl = "https://github.com/download/arm64.apk",
                    size = 1234,
                    digest = "sha256:deadbeef",
                ),
                GitHubAssetDto(
                    name = "MyTestflight-1.4.0-universal.apk",
                    browserDownloadUrl = "https://github.com/download/universal.apk",
                    size = 4321,
                ),
            ),
        )

        assertEquals(setOf("arm64-v8a", "universal"), assets.keys)
        val arm64 = assets.getValue("arm64-v8a")
        assertEquals("deadbeef", arm64.sha256)
        assertEquals(1234, arm64.sizeBytes)
        assertEquals("https://github.com/download/arm64.apk", arm64.url)
        assertNull(assets.getValue("universal").sha256)
    }
}
