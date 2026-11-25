package com.mycompany.testflight.data.di

import com.mycompany.testflight.data.device.AndroidDeviceProfileProvider
import com.mycompany.testflight.data.installer.ApkInstallerImpl
import com.mycompany.testflight.data.installer.IntentAppLauncher
import com.mycompany.testflight.data.installed.PackageManagerInstalledAppRepository
import com.mycompany.testflight.data.repository.AppRepositoryImpl
import com.mycompany.testflight.data.settings.ServerSettingsRepositoryImpl
import com.mycompany.testflight.data.settings.ServerUrlStore
import com.mycompany.testflight.data.settings.SharedPreferencesServerUrlStore
import com.mycompany.testflight.data.update.PackageManagerAppInfoProvider
import com.mycompany.testflight.data.update.SharedPreferencesUpdateLocalStore
import com.mycompany.testflight.data.update.UpdateRepositoryImpl
import com.mycompany.testflight.domain.device.DeviceProfileProvider
import com.mycompany.testflight.domain.installer.ApkInstaller
import com.mycompany.testflight.domain.installer.AppLauncher
import com.mycompany.testflight.domain.repository.AppRepository
import com.mycompany.testflight.domain.repository.InstalledAppRepository
import com.mycompany.testflight.domain.settings.ServerSettingsRepository
import com.mycompany.testflight.domain.update.AppInfoProvider
import com.mycompany.testflight.domain.update.UpdateLocalStore
import com.mycompany.testflight.domain.update.UpdateRepository
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
    abstract fun bindAppLauncher(impl: IntentAppLauncher): AppLauncher

    @Binds
    @Singleton
    abstract fun bindInstalledAppRepository(
        impl: PackageManagerInstalledAppRepository,
    ): InstalledAppRepository

    @Binds
    @Singleton
    abstract fun bindDeviceProfileProvider(
        impl: AndroidDeviceProfileProvider,
    ): DeviceProfileProvider

    @Binds
    @Singleton
    abstract fun bindServerUrlStore(impl: SharedPreferencesServerUrlStore): ServerUrlStore

    @Binds
    @Singleton
    abstract fun bindServerSettingsRepository(
        impl: ServerSettingsRepositoryImpl,
    ): ServerSettingsRepository

    @Binds
    @Singleton
    abstract fun bindUpdateRepository(impl: UpdateRepositoryImpl): UpdateRepository

    @Binds
    @Singleton
    abstract fun bindUpdateLocalStore(
        impl: SharedPreferencesUpdateLocalStore,
    ): UpdateLocalStore

    @Binds
    @Singleton
    abstract fun bindAppInfoProvider(impl: PackageManagerAppInfoProvider): AppInfoProvider
}
