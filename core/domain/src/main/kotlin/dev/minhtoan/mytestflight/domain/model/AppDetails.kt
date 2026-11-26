package dev.minhtoan.mytestflight.domain.model

data class AppDetails(
    val app: TestApp,
    val versions: List<AppVersion>,
)
