package com.mycompany.testflight.data.di

import com.mycompany.testflight.data.update.GitHubReleaseApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * GitHub's API is a different origin from the configurable catalog server, so it gets its own
 * client: the catalog client's [BaseUrlInterceptor][com.mycompany.testflight.data.remote.BaseUrlInterceptor]
 * would rewrite api.github.com to the user's server origin.
 */
@Module
@InstallIn(SingletonComponent::class)
object UpdateModule {

    private const val GITHUB_API_BASE_URL = "https://api.github.com/"
    private const val USER_AGENT = "MyTestflight-Android"

    @Provides
    @Singleton
    @Named("githubClient")
    fun provideGitHubClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", USER_AGENT)
                .header("X-GitHub-Api-Version", "2022-11-28")
                .build()
            chain.proceed(request)
        }
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    @Named("githubRetrofit")
    fun provideGitHubRetrofit(
        @Named("githubClient") client: OkHttpClient,
        json: Json,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(GITHUB_API_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideGitHubReleaseApi(
        @Named("githubRetrofit") retrofit: Retrofit,
    ): GitHubReleaseApi = retrofit.create(GitHubReleaseApi::class.java)
}
