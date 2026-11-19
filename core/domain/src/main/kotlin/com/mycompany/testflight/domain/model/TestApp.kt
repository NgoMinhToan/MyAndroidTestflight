package com.mycompany.testflight.domain.model

data class TestApp(
    val id: String,
    val name: String,
    val packageName: String,
    val iconUrl: String?,
    val latestVersion: AppVersion?,
)
