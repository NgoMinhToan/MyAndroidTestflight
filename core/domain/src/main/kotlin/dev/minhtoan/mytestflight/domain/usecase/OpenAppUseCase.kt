package dev.minhtoan.mytestflight.domain.usecase

import dev.minhtoan.mytestflight.domain.installer.AppLauncher
import javax.inject.Inject

class OpenAppUseCase @Inject constructor(private val launcher: AppLauncher) {
    operator fun invoke(packageName: String): Boolean = launcher.open(packageName)
}
