package com.mycompany.testflight.domain.repository

import com.mycompany.testflight.domain.model.AppDetails
import com.mycompany.testflight.domain.model.TestApp

interface AppRepository {
    suspend fun getApps(): Result<List<TestApp>>
    suspend fun getApp(id: String): Result<AppDetails>
}
