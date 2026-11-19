package com.mycompany.testflight.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AppsResponseDto(
    val apps: List<AppDto> = emptyList(),
)

@Serializable
data class AppDto(
    val id: String,
    val name: String = "",
    val packageName: String = "",
    val iconPath: String? = null,
    val latestVersion: VersionDto? = null,
)

@Serializable
data class VersionDto(
    val versionName: String = "",
    val versionCode: Int = 0,
    val minSdk: Int? = null,
    val releaseNotes: String? = null,
    val publishedAt: String = "",
    val sizeBytes: Long = 0L,
    val sha256: String? = null,
    val downloadPath: String = "",
)
