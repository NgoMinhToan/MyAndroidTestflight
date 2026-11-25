package com.mycompany.testflight.domain.model

/** One downloadable APK for a version, tagged with the ABI it targets. */
data class ApkArtifact(
    val abi: String,
    val sizeBytes: Long,
    val sha256: String?,
    val downloadPath: String,
) {
    companion object {
        const val UNIVERSAL_ABI: String = "universal"
    }
}
