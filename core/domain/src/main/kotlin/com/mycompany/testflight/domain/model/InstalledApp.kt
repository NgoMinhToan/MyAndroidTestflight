package com.mycompany.testflight.domain.model

/** The copy of an app currently installed on this device, if any. */
data class InstalledApp(
    val packageName: String,
    val versionCode: Long,
    val versionName: String? = null,
)
