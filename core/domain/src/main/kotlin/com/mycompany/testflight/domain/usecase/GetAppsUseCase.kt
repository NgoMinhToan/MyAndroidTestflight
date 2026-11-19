package com.mycompany.testflight.domain.usecase

import com.mycompany.testflight.domain.model.TestApp
import com.mycompany.testflight.domain.repository.AppRepository
import javax.inject.Inject

class GetAppsUseCase @Inject constructor(private val repository: AppRepository) {
    suspend operator fun invoke(): Result<List<TestApp>> = repository.getApps()
}
