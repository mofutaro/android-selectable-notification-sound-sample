package com.mofuapps.selectablenotificationsound.di

import android.content.Context
import com.mofuapps.selectablenotificationsound.data.AppDatabase
import com.mofuapps.selectablenotificationsound.data.getDatabase
import com.mofuapps.selectablenotificationsound.data.session.RealSessionRepository
import com.mofuapps.selectablenotificationsound.data.session.SessionDao
import com.mofuapps.selectablenotificationsound.domain.session.SessionRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun bindSessionRepository(repository: RealSessionRepository): SessionRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return getDatabase(context)
    }

    @Provides
    fun provideSessionDao(database: AppDatabase): SessionDao = database.sessionDao
}
