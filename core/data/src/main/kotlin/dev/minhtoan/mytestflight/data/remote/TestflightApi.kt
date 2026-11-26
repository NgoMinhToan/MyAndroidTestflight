package dev.minhtoan.mytestflight.data.remote

import dev.minhtoan.mytestflight.data.remote.dto.AppResponseDto
import dev.minhtoan.mytestflight.data.remote.dto.AppsResponseDto
import retrofit2.http.GET
import retrofit2.http.Path

interface TestflightApi {
    @GET("api/v1/apps")
    suspend fun getApps(): AppsResponseDto

    @GET("api/v1/apps/{appId}")
    suspend fun getApp(@Path("appId") appId: String): AppResponseDto
}
