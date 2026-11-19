package com.mycompany.testflight.data.remote

import com.mycompany.testflight.data.remote.dto.AppsResponseDto
import retrofit2.http.GET

interface TestflightApi {
    @GET("api/v1/apps")
    suspend fun getApps(): AppsResponseDto
}
