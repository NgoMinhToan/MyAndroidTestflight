package dev.minhtoan.mytestflight.di

import dev.minhtoan.mytestflight.domain.update.UpdateController
import dev.minhtoan.mytestflight.update.UpdateControllerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UpdateBindingsModule {

    @Binds
    @Singleton
    abstract fun bindUpdateController(impl: UpdateControllerImpl): UpdateController
}
