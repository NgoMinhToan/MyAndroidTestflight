package com.mycompany.testflight.domain.usecase

import com.mycompany.testflight.domain.installer.AppLauncher
import javax.inject.Inject

class OpenAppUseCase @Inject constructor(private val launcher: AppLauncher) {
    operator fun invoke(packageName: String): Boolean = launcher.open(packageName)
}
