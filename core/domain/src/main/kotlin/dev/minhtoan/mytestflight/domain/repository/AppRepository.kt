package dev.minhtoan.mytestflight.domain.repository

import dev.minhtoan.mytestflight.domain.model.AppDetails
import dev.minhtoan.mytestflight.domain.model.TestApp

interface AppRepository {
    suspend fun getApps(): Result<List<TestApp>>
    suspend fun getApp(id: String): Result<AppDetails>
}
