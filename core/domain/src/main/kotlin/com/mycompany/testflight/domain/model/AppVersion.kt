package com.mycompany.testflight.domain.model

data class AppVersion(
    val versionName: String,
    val versionCode: Int,
    val minSdk: Int?,
    val releaseNotes: String?,
    val publishedAt: String,
    val sizeBytes: Long,
    val sha256: String?,
    val downloadPath: String,
    /**
     * Per-ABI downloads published with this version. Empty for manifests written before
     * per-ABI artifacts existed; build resolution then treats [downloadPath] as universal.
     */
    val artifacts: List<ApkArtifact> = emptyList(),
)
