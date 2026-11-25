package com.mycompany.testflight.data.update

import com.mycompany.testflight.domain.update.ReleaseAsset

/** Turns GitHub asset metadata into the domain model, inferring the ABI from the file name. */
object GitHubReleaseMapper {

    private val knownAbis = listOf(
        "arm64-v8a",
        "armeabi-v7a",
        "armeabi",
        "x86_64",
        "x86",
        "riscv64",
        "mips64",
        "mips",
        "universal",
    )

    fun assetsFromApi(assets: List<GitHubAssetDto>): Map<String, ReleaseAsset> =
        assets.mapNotNull { asset ->
            val abi = abiFromName(asset.name) ?: return@mapNotNull null
            abi to ReleaseAsset(
                abi = abi,
                name = asset.name,
                url = asset.browserDownloadUrl,
                sizeBytes = asset.size,
                sha256 = asset.digest
                    ?.removePrefix("sha256:")
                    ?.takeIf { it.isNotBlank() },
            )
        }.toMap()

    fun abiFromName(name: String): String? {
        val lower = name.lowercase()
        if (!lower.endsWith(".apk")) return null
        return knownAbis.firstOrNull { lower.endsWith("-$it.apk") }
            ?: lower.removeSuffix(".apk")
                .substringAfterLast('-', missingDelimiterValue = "")
                .takeIf { it.isNotBlank() }
    }
}
