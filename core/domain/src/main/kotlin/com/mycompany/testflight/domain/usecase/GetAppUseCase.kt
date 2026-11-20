package com.mycompany.testflight.domain.usecase

import com.mycompany.testflight.domain.model.AppDetails
import com.mycompany.testflight.domain.repository.AppRepository
import javax.inject.Inject

class GetAppUseCase @Inject constructor(private val repository: AppRepository) {
    suspend operator fun invoke(id: String): Result<AppDetails> = repository.getApp(id)
}
