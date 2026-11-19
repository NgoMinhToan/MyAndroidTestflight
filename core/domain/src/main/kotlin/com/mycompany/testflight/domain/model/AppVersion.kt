package com.mycompany.testflight.domain.model

data class AppVersion(
    val versionName: String,
    val versionCode: Int,
    val minSdk: Int?,
    val releaseNotes: String?,
    val publishedAt: String,
    val sizeBytes: Long,
    val sha256: String?,
    val downloadUrl: String,
)
