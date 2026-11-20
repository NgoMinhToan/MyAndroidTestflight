pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MyAndroidTestflight"

include(":app")
include(":core:domain")
include(":core:data")
include(":core:designsystem")
include(":feature:applist")
include(":feature:settings")
