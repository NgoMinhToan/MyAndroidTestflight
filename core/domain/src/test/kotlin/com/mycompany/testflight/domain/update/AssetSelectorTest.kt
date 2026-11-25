package com.mycompany.testflight.domain.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AssetSelectorTest {

    private fun asset(abi: String): ReleaseAsset = ReleaseAsset(
        abi = abi,
        name = "MyTestflight-1.2.3-$abi.apk",
        url = "https://example.com/$abi.apk",
        sizeBytes = 1_000,
        sha256 = null,
    )

    @Test
    fun `picks the first device abi that has an asset`() {
        val assets = mapOf(
            "arm64-v8a" to asset("arm64-v8a"),
            "x86_64" to asset("x86_64"),
            "universal" to asset("universal"),
        )

        val selected = AssetSelector.select(assets, listOf("x86_64", "arm64-v8a"))

        assertEquals("x86_64", selected?.abi)
    }

    @Test
    fun `falls back to a prefix match before universal`() {
        val assets = mapOf(
            "arm64" to asset("arm64"),
            "universal" to asset("universal"),
        )

        val selected = AssetSelector.select(assets, listOf("arm64-v8a"))

        assertEquals("arm64", selected?.abi)
    }

    @Test
    fun `falls back to universal when no abi matches`() {
        val assets = mapOf(
            "arm64-v8a" to asset("arm64-v8a"),
            "universal" to asset("universal"),
        )

        val selected = AssetSelector.select(assets, listOf("mips"))

        assertEquals("universal", selected?.abi)
    }

    @Test
    fun `returns null when nothing is usable`() {
        assertNull(AssetSelector.select(emptyMap(), listOf("arm64-v8a")))
        assertNull(AssetSelector.select(mapOf("x86_64" to asset("x86_64")), listOf("mips")))
    }

    @Test
    fun `asset keys are matched case insensitively`() {
        val assets = mapOf("ARM64-V8A" to asset("ARM64-V8A"))

        assertEquals("ARM64-V8A", AssetSelector.select(assets, listOf("arm64-v8a"))?.abi)
    }
}
