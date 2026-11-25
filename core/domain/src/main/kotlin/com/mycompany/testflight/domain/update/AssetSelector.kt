package com.mycompany.testflight.domain.update

/**
 * Picks the APK that matches the device. Device ABIs are in priority order (Android reports the
 * primary ABI first); an exact match wins, then a prefix match (for example "arm64" covering
 * "arm64-v8a"), and finally the universal APK.
 */
object AssetSelector {

    fun select(
        assets: Map<String, ReleaseAsset>,
        deviceAbis: List<String>,
    ): ReleaseAsset? {
        if (assets.isEmpty()) return null
        val byAbi = assets.entries.associate { it.key.lowercase() to it.value }

        deviceAbis.forEach { abi ->
            byAbi[abi.lowercase()]?.let { return it }
        }

        deviceAbis.forEach { abi ->
            val normalized = abi.lowercase()
            byAbi.entries.firstOrNull { (key, _) ->
                key != UNIVERSAL && normalized.startsWith(key)
            }?.let { return it.value }
        }

        return byAbi[UNIVERSAL]
            ?: assets.values.firstOrNull { it.abi.equals(UNIVERSAL, ignoreCase = true) }
    }

    private const val UNIVERSAL = "universal"
}
