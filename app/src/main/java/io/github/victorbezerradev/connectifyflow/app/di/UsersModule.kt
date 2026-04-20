package io.github.victorbezerradev.connectifyflow.app.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.victorbezerradev.connectifyflow.modules.users.domain.coordinators.HeartbeatCoordinatorImpl
import io.github.victorbezerradev.connectifyflow.modules.users.domain.interfaces.HeartbeatCoordinator
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UsersModule {
    @Binds
    @Singleton
    abstract fun bindHeartbeatCoordinator(impl: HeartbeatCoordinatorImpl): HeartbeatCoordinator
}
