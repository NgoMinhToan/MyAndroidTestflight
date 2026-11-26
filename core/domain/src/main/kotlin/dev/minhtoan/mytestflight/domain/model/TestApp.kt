package dev.minhtoan.mytestflight.domain.model

data class TestApp(
    val id: String,
    val name: String,
    val packageName: String,
    val iconUrl: String?,
    val latestVersion: AppVersion?,
    val description: String = "",
    val developer: String = "",
    val versionCount: Int = 0,
)
