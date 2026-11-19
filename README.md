# MyAndroidTestflight

Kotlin 2.1.10 + Jetpack Compose client for the internal TestFlight server. One screen lists the
catalog; Install downloads the APK, verifies its optional SHA-256, and opens the Android package
installer.

## Modules

| Module | Responsibility |
|---|---|
| `:app` | `TestflightApplication`, `MainActivity`, manifest, launcher icon |
| `:core:domain` | models, `AppRepository`, `ApkInstaller`, use cases, `AppError` |
| `:core:data` | Retrofit API/DTOs, repository implementation, APK downloader, Hilt modules, FileProvider |
| `:core:designsystem` | `MyTestflightTheme` |
| `:feature:applist` | screen, ViewModel, UI state/events, unit tests |

## Build and test

```bash
./gradlew :feature:applist:testDebugUnitTest
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

A debug build is produced at `app/build/outputs/apk/debug/app-debug.apk`.

## Server URL

Resolution order (first match wins):

1. Gradle property: `./gradlew :app:assembleDebug -Pserver_url=http://127.0.0.1:8080`
2. `server_url` in `local.properties` (not committed; copy `local.properties.example`)
3. `http://10.0.2.2:8080`

Trimmed and validated as `^https?://\S+$`; a bad value fails configuration with a clear message.

- Emulator → `10.0.2.2` reaches the host.
- USB device → `adb reverse tcp:8080 tcp:8080` and use `http://127.0.0.1:8080`.
- Wi-Fi device → use the host's LAN address.

`local.properties` must also contain `sdk.dir` for local builds.

## Install flow

1. `canRequestPackageInstalls()` is checked first; if denied, the app opens
   `Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES` for its own package.
2. The APK is streamed into `cacheDir/apks/` with 64 KiB chunks; progress is emitted by whole
   percent or every 100 ms.
3. The declared SHA-256 (if any) is verified; mismatches delete the file.
4. The file is exposed through `FileProvider` (authority `${applicationId}.fileprovider`, path
   `apks/`) and opened with `ACTION_VIEW` + `application/vnd.android.package-archive`.

The final status means the installer UI opened, not that the user completed installation.

Cleartext HTTP is enabled for this internal build.

## CI

`.github/workflows/android-ci.yml` runs the unit tests, assembles the debug APK on JDK 17, and
uploads it as the `my-android-testflight-debug` artifact. There is no signing/release step.
