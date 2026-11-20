# MyAndroidTestflight

Kotlin 2.4.21 + Jetpack Compose client for the internal TestFlight server. The app has an Apps tab
with search, a detail screen that lists every published version, and a Settings tab for the server
URL. Install downloads the APK from the current server, verifies its optional SHA-256, and opens
the Android package installer. The UI follows the approved "Friendly" design (coral header, white
sheets, pastel app tiles, Plus Jakarta Sans + IBM Plex Mono) and supports predictive back with an
iOS-style slide transition.

## Modules

| Module | Responsibility |
|---|---|
| `:app` | application, `MainActivity`, bottom navigation/NavHost, application-scope Hilt module, launcher icon |
| `:core:domain` | models, repositories, `ApkInstaller`, use cases, typed errors, application-scope qualifier |
| `:core:data` | Retrofit API/DTOs, repository/installer implementations, runtime server settings, BaseUrlInterceptor, Hilt modules, FileProvider |
| `:core:designsystem` | `MyTestflightTheme`, Friendly tokens/components, bundled fonts, `UiText` resource abstraction |
| `:feature:applist` | catalog + detail screens, search, ViewModels, install coordinator, unit tests |
| `:feature:settings` | settings screen/ViewModel for the server URL, unit tests |

## Build and test

Requires JDK 17+ (AGP 9.5 / Gradle 9.9) and Android SDK platform 37.2
(`sdkmanager "platforms;android-37.2"`). If your shell default is older, run
`JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew …` on macOS.

```bash
./gradlew :core:domain:test :core:data:testDebugUnitTest \
  :feature:applist:testDebugUnitTest :feature:settings:testDebugUnitTest \
  :app:testDebugUnitTest :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Screens

- **Apps** — searchable catalog (name, package, description, developer; Vietnamese diacritics are
  ignored). Pull to refresh. Tapping a row opens the detail screen; the install pill and progress
  line stay in the row.
- **App details** — coral/pastel header with the app's accent, description, developer, version count,
  and every version newest-first with version code, min SDK, size, localized publish date, release
  notes, a Latest badge, and an install action per version. Predictive back and the header back
  button slide the screen to the right over a parallaxed list. Downloads continue while navigating
  between screens because `InstallCoordinator` runs on an application-scoped coroutine.
- **Settings** — edits the server URL, validates it, restores the build default, and shows inline
  errors in the current locale.

The UI is localized in English (`values/`) and Vietnamese (`values-vi/`), follows the system
locale, and declares `locales_config.xml` so Android 13+ also allows per-app language selection.
User-visible text lives in string resources; errors travel from domain/data as typed values and are
rendered through `UiText`.

A debug build is produced at `app/build/outputs/apk/debug/app-debug.apk`.

## Server URL

The URL has a build-time default and a runtime override:

1. **Runtime (Settings tab):** saved in `SharedPreferences` (`server_settings` / `server_url`) and
   applied immediately through `BaseUrlInterceptor`; no app restart is needed. The catalog and
   detail screens reload on change. Reset removes the preference and falls back to the build default.
2. **Build default** (first match wins):
   - Gradle property: `./gradlew :app:assembleDebug -Pserver_url=http://127.0.0.1:8080`
   - `server_url` in `local.properties` (not committed; copy `local.properties.example`)
   - `http://10.0.2.2:8080`

Both paths accept only an `http(s)` **origin** — no path, query, fragment, or credentials. The
build-time regex and the runtime validator enforce this. Icons resolve against the current URL,
and downloads resolve the version's `downloadPath` against it when the install starts.

- Emulator → `10.0.2.2` reaches the host.
- USB device → `adb reverse tcp:8080 tcp:8080` and use `http://127.0.0.1:8080`.
- Wi-Fi device → use the host's LAN address.

`local.properties` must also contain `sdk.dir` for local builds.

## Install flow

1. `canRequestPackageInstalls()` is checked first; if denied, the app opens
   `Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES` for its own package.
2. The version's relative `downloadPath` is resolved against the current server URL and streamed
   into `cacheDir/apks/` with 64 KiB chunks; progress is emitted by whole percent or every 100 ms.
3. The declared SHA-256 (if any) is verified; mismatches delete the file.
4. The file is exposed through `FileProvider` (authority `${applicationId}.fileprovider`, path
   `apks/`) and opened with `ACTION_VIEW` + `application/vnd.android.package-archive`.

The final status means the installer UI opened, not that the user completed installation.
Failures are typed (`InstallFailure`) and localized by the UI layer.

Cleartext HTTP is enabled for this internal build.

## CI

`.github/workflows/android-ci.yml` runs `:core:domain:test` plus every module's debug unit tests,
assembles the debug APK on JDK 17, and uploads it as the `my-android-testflight-debug` artifact.

## Releases

`.github/workflows/android-release.yml` builds a marked version and publishes it:

| Trigger | Version | Result |
|---|---|---|
| push a tag `v1.2.3` | `1.2.3` from the tag | release `v1.2.3` |
| "Run workflow" with `version` | that version | tag + release for it |
| "Run workflow" without `version` | `versionName` in `app/build.gradle.kts` | tag + release for it |

Each run produces, as GitHub release assets and workflow artifacts:

```
MyTestflight-<version>-arm64-v8a.apk      per-ABI APK (arm64-v8a, armeabi-v7a, x86_64)
MyTestflight-<version>-universal.apk      one APK for every ABI
MyTestflight-<version>.aab                Android App Bundle
SHA-256SUMS.txt                           checksums for all of the above
```

`versionCode` is the commit count, so it always grows and devices upgrade in place. Versions with
a pre-release suffix (`v1.2.3-beta.1`) publish as pre-releases; re-running a version replaces the
existing release assets.

Splits are driven by Gradle properties, so the same artifacts can be built locally:

```bash
./gradlew :app:assembleRelease :app:bundleRelease \
  -PappVersionName=1.2.3 -PappVersionCode=42 \
  -PappAbiSplits=true -PappAbiFilters="arm64-v8a,armeabi-v7a,x86_64" -PappUniversalApk=true
```

Release signing reads `RELEASE_STORE_FILE`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, and
`RELEASE_KEY_PASSWORD` from the environment or same-named Gradle properties. The workflow fills
them from the `ANDROID_KEYSTORE_BASE64`, `ANDROID_STORE_PASSWORD`, `ANDROID_KEY_ALIAS`, and
`ANDROID_KEY_PASSWORD` repository secrets. Without them, release artifacts are signed with the
debug key: installable for testing, but not suitable for distribution. Keep the keystore secret and
never commit it.
