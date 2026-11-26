plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// The defaults live in gradle.properties (appVersionName / appVersionCode) so the release
// workflow can read them; -P overrides stamp a version without editing any file.
val appVersionName = providers.gradleProperty("appVersionName").orNull
    ?.trim()?.takeIf { it.isNotEmpty() } ?: "0.1.0"
val appVersionCode = providers.gradleProperty("appVersionCode").orNull
    ?.toIntOrNull() ?: 1

// ABI splits stay off for local builds. The release workflow enables them and also asks for a
// universal APK, so testers can take a small per-architecture download or one that always works.
val abiFilters = providers.gradleProperty("appAbiFilters").orNull
    ?.split(',')?.map { it.trim() }?.filter { it.isNotEmpty() }
    .orEmpty()
val abiSplitsEnabled = providers.gradleProperty("appAbiSplits").orNull?.toBooleanStrictOrNull()
    ?: abiFilters.isNotEmpty()
val universalApkEnabled = providers.gradleProperty("appUniversalApk").orNull
    ?.toBooleanStrictOrNull() ?: true

// Release signing material comes from Gradle properties or environment variables (CI secrets).
// Without it a release build is signed with the debug key, so artifacts stay installable.
fun signingValue(property: String, environment: String): String? =
    providers.gradleProperty(property).orNull?.takeIf { it.isNotBlank() }
        ?: providers.environmentVariable(environment).orNull?.takeIf { it.isNotBlank() }

val releaseStoreFile = signingValue("releaseStoreFile", "RELEASE_STORE_FILE")
val releaseStorePassword = signingValue("releaseStorePassword", "RELEASE_STORE_PASSWORD")
val releaseKeyAlias = signingValue("releaseKeyAlias", "RELEASE_KEY_ALIAS")
val releaseKeyPassword = signingValue("releaseKeyPassword", "RELEASE_KEY_PASSWORD")

android {
    namespace = "dev.minhtoan.mytestflight"
    compileSdk = 37
    compileSdkMinor = 2

    defaultConfig {
        applicationId = "dev.minhtoan.mytestflight"
        minSdk = 26
        targetSdk = 37
        versionCode = appVersionCode
        versionName = appVersionName
    }

    signingConfigs {
        if (releaseStoreFile != null) {
            create("release") {
                storeFile = file(releaseStoreFile)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    splits {
        abi {
            isEnable = abiSplitsEnabled
            reset()
            include(
                *(if (abiFilters.isEmpty()) listOf("arm64-v8a", "armeabi-v7a", "x86_64") else abiFilters)
                    .toTypedArray(),
            )
            isUniversalApk = universalApkEnabled
        }
    }

    buildTypes {
        release {
            // R8 shrinks and optimizes code; resource shrinking removes unused resources. Both are
            // off for debug builds, so unit tests and day-to-day builds stay fast.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:domain"))
    implementation(project(":feature:applist"))
    implementation(project(":feature:settings"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
