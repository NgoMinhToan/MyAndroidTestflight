package dev.minhtoan.mytestflight.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AppsResponseDto(
    val apps: List<AppDto> = emptyList(),
)

@Serializable
data class AppResponseDto(
    val app: AppDto,
)

@Serializable
data class AppDto(
    val id: String,
    val name: String = "",
    val packageName: String = "",
    val description: String = "",
    val developer: String = "",
    val versionCount: Int = 0,
    val iconPath: String? = null,
    val latestVersion: VersionDto? = null,
    val versions: List<VersionDto> = emptyList(),
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
    val artifacts: List<ArtifactDto> = emptyList(),
)

@Serializable
data class ArtifactDto(
    val abi: String = "",
    val sizeBytes: Long = 0L,
    val sha256: String? = null,
    val downloadPath: String = "",
)
