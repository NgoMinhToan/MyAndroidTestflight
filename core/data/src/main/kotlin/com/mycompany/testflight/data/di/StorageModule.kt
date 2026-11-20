package com.mycompany.testflight.data.di

import com.mycompany.testflight.data.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {

    @Provides
    @Singleton
    @Named("defaultServerUrl")
    fun provideDefaultServerUrl(): String = BuildConfig.SERVER_URL.trimEnd('/')
}
