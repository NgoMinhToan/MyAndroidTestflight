package com.mycompany.testflight.domain.usecase

import com.mycompany.testflight.domain.update.AppRelease
import com.mycompany.testflight.domain.update.UpdateDownload
import com.mycompany.testflight.domain.update.UpdateRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class DownloadUpdateUseCase @Inject constructor(private val repository: UpdateRepository) {
    operator fun invoke(release: AppRelease): Flow<UpdateDownload> = repository.download(release)
}
