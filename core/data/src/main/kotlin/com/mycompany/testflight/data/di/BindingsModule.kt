package com.mycompany.testflight.data.di

import com.mycompany.testflight.data.installer.ApkInstallerImpl
import com.mycompany.testflight.data.repository.AppRepositoryImpl
import com.mycompany.testflight.data.settings.ServerSettingsRepositoryImpl
import com.mycompany.testflight.data.settings.ServerUrlStore
import com.mycompany.testflight.data.settings.SharedPreferencesServerUrlStore
import com.mycompany.testflight.domain.installer.ApkInstaller
import com.mycompany.testflight.domain.repository.AppRepository
import com.mycompany.testflight.domain.settings.ServerSettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BindingsModule {

    @Binds
    @Singleton
    abstract fun bindAppRepository(impl: AppRepositoryImpl): AppRepository

    @Binds
    @Singleton
    abstract fun bindApkInstaller(impl: ApkInstallerImpl): ApkInstaller

    @Binds
    @Singleton
    abstract fun bindServerUrlStore(impl: SharedPreferencesServerUrlStore): ServerUrlStore

    @Binds
    @Singleton
    abstract fun bindServerSettingsRepository(
        impl: ServerSettingsRepositoryImpl,
    ): ServerSettingsRepository
}
