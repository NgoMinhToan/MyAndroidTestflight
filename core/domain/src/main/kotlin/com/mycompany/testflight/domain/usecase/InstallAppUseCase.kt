package com.mycompany.testflight.domain.usecase

import com.mycompany.testflight.domain.apk.ResolvedBuild
import com.mycompany.testflight.domain.installer.ApkInstaller
import com.mycompany.testflight.domain.installer.InstallStatus
import com.mycompany.testflight.domain.model.TestApp
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class InstallAppUseCase @Inject constructor(private val installer: ApkInstaller) {
    operator fun invoke(app: TestApp, build: ResolvedBuild): Flow<InstallStatus> =
        installer.install(app, build)
}
