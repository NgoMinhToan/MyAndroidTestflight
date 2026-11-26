package dev.minhtoan.mytestflight.data.di

import dev.minhtoan.mytestflight.data.device.AndroidDeviceProfileProvider
import dev.minhtoan.mytestflight.data.installer.ApkInstallerImpl
import dev.minhtoan.mytestflight.data.installer.IntentAppLauncher
import dev.minhtoan.mytestflight.data.installed.PackageManagerInstalledAppRepository
import dev.minhtoan.mytestflight.data.repository.AppRepositoryImpl
import dev.minhtoan.mytestflight.data.settings.ServerSettingsRepositoryImpl
import dev.minhtoan.mytestflight.data.settings.ServerUrlStore
import dev.minhtoan.mytestflight.data.settings.SharedPreferencesServerUrlStore
import dev.minhtoan.mytestflight.data.update.PackageManagerAppInfoProvider
import dev.minhtoan.mytestflight.data.update.SharedPreferencesUpdateLocalStore
import dev.minhtoan.mytestflight.data.update.UpdateRepositoryImpl
import dev.minhtoan.mytestflight.domain.device.DeviceProfileProvider
import dev.minhtoan.mytestflight.domain.installer.ApkInstaller
import dev.minhtoan.mytestflight.domain.installer.AppLauncher
import dev.minhtoan.mytestflight.domain.repository.AppRepository
import dev.minhtoan.mytestflight.domain.repository.InstalledAppRepository
import dev.minhtoan.mytestflight.domain.settings.ServerSettingsRepository
import dev.minhtoan.mytestflight.domain.update.AppInfoProvider
import dev.minhtoan.mytestflight.domain.update.UpdateLocalStore
import dev.minhtoan.mytestflight.domain.update.UpdateRepository
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
