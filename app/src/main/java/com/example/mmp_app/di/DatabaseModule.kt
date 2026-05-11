package com.example.mmp_app.di

import android.content.Context
import androidx.room.Room
import com.example.mmp_app.data.local.MmpDatabase
import com.example.mmp_app.data.local.dao.DashboardDao
import com.example.mmp_app.data.local.dao.UserProfileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MmpDatabase {
        return Room.databaseBuilder(
            context,
            MmpDatabase::class.java,
            "mmp_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideUserProfileDao(database: MmpDatabase): UserProfileDao {
        return database.userProfileDao()
    }

    @Provides
    fun provideDashboardDao(database: MmpDatabase): DashboardDao {
        return database.dashboardDao()
    }
}
