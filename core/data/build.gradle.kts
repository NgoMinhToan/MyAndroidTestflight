import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

val serverUrl: String = providers.gradleProperty("server_url").orNull
    ?: rootProject.file("local.properties")
        .takeIf { it.isFile }
        ?.let { file ->
            Properties().apply { file.inputStream().use { load(it) } }.getProperty("server_url")
        }
    ?: "http://10.0.2.2:8080"

val normalizedServerUrl = serverUrl.trim().trimEnd('/')
require(Regex("^https?://\\S+$").matches(normalizedServerUrl)) {
    "Invalid server_url \"$serverUrl\": expected an http(s) URL, for example http://10.0.2.2:8080"
}

android {
    namespace = "com.mycompany.testflight.data"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        buildConfigField("String", "SERVER_URL", "\"$normalizedServerUrl\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core:domain"))

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.core.ktx)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
