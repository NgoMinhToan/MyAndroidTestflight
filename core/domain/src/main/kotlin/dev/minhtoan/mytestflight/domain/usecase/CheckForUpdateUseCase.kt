package dev.minhtoan.mytestflight.domain.usecase

import dev.minhtoan.mytestflight.domain.update.UpdateCheck
import dev.minhtoan.mytestflight.domain.update.UpdateRepository
import javax.inject.Inject

class CheckForUpdateUseCase @Inject constructor(private val repository: UpdateRepository) {
    suspend operator fun invoke(): Result<UpdateCheck> = repository.checkForUpdate()
}
