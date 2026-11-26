package dev.minhtoan.mytestflight.domain.usecase

import dev.minhtoan.mytestflight.domain.apk.ResolvedBuild
import dev.minhtoan.mytestflight.domain.installer.ApkInstaller
import dev.minhtoan.mytestflight.domain.installer.InstallStatus
import dev.minhtoan.mytestflight.domain.model.TestApp
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class InstallAppUseCase @Inject constructor(private val installer: ApkInstaller) {
    operator fun invoke(app: TestApp, build: ResolvedBuild): Flow<InstallStatus> =
        installer.install(app, build)
}
