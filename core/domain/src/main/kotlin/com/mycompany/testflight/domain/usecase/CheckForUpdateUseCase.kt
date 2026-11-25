package com.mycompany.testflight.domain.usecase

import com.mycompany.testflight.domain.update.UpdateCheck
import com.mycompany.testflight.domain.update.UpdateRepository
import javax.inject.Inject

class CheckForUpdateUseCase @Inject constructor(private val repository: UpdateRepository) {
    suspend operator fun invoke(): Result<UpdateCheck> = repository.checkForUpdate()
}
