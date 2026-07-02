package com.example.mmp_app.data.repository

import com.example.mmp_app.domain.repository.AuthRepository
import com.example.mmp_app.domain.repository.DashboardRepository
import com.example.mmp_app.domain.repository.NotificationRepository
import com.example.mmp_app.domain.repository.ParentRepository
import com.example.mmp_app.domain.repository.SettingsRepository
import dagger.Binds

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindDashboardRepository(
        dashboardRepositoryImpl: DashboardRepositoryImpl
    ): DashboardRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        notificationRepositoryImpl: NotificationRepositoryImpl
    ): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindParentRepository(
        parentRepositoryImpl: ParentRepositoryImpl
    ): ParentRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository
}
