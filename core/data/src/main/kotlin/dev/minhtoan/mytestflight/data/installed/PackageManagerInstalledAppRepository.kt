package dev.minhtoan.mytestflight.data.installed

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import dev.minhtoan.mytestflight.domain.model.InstalledApp
import dev.minhtoan.mytestflight.domain.repository.InstalledAppRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class PackageManagerInstalledAppRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : InstalledAppRepository {

    override suspend fun installedApps(
        packageNames: Collection<String>,
    ): Map<String, InstalledApp> = withContext(Dispatchers.IO) {
        val packageManager = context.packageManager
        packageNames.asSequence()
            .filter { it.isNotBlank() }
            .distinct()
            .mapNotNull { packageName ->
                packageInfo(packageManager, packageName)?.let { info ->
                    InstalledApp(
                        packageName = packageName,
                        versionCode = info.versionCodeOnDevice(),
                        versionName = info.versionName,
                    )
                }
            }
            .associateBy { it.packageName }
    }

    private fun packageInfo(
        packageManager: PackageManager,
        packageName: String,
    ): PackageInfo? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, 0)
        }
    } catch (error: PackageManager.NameNotFoundException) {
        null
    } catch (error: SecurityException) {
        // Package visibility can still hide an app on unusual configurations.
        null
    }

    private fun PackageInfo.versionCodeOnDevice(): Long =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            longVersionCode
        } else {
            @Suppress("DEPRECATION")
            versionCode.toLong()
        }
}
