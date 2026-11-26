package dev.minhtoan.mytestflight.domain.usecase

import dev.minhtoan.mytestflight.domain.update.AppRelease
import dev.minhtoan.mytestflight.domain.update.UpdateDownload
import dev.minhtoan.mytestflight.domain.update.UpdateRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class DownloadUpdateUseCase @Inject constructor(private val repository: UpdateRepository) {
    operator fun invoke(release: AppRelease): Flow<UpdateDownload> = repository.download(release)
}
