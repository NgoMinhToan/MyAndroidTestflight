package com.mycompany.testflight.di

import com.mycompany.testflight.domain.update.UpdateController
import com.mycompany.testflight.update.UpdateControllerImpl
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
