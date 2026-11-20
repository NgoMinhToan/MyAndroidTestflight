package com.mycompany.testflight.domain.model

data class AppDetails(
    val app: TestApp,
    val versions: List<AppVersion>,
)
