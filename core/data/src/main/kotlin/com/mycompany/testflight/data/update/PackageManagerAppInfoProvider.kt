package com.mycompany.testflight.data.update

import android.content.Context
import android.os.Build
import com.mycompany.testflight.domain.update.AppInfoProvider
import com.mycompany.testflight.domain.update.AppVersionInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PackageManagerAppInfoProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) : AppInfoProvider {

    override val version: AppVersionInfo by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        AppVersionInfo(
            versionName = info.versionName.orEmpty(),
            versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                info.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                info.versionCode.toLong()
            },
        )
    }
}
