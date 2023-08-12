package com.mofuapps.selectablenotificationsound.di

import android.content.Context
import com.mofuapps.selectablenotificationsound.domain.alarm.NotifyZeroAlarmManager
import com.mofuapps.selectablenotificationsound.domain.notification.AlarmNotificationManager
import com.mofuapps.selectablenotificationsound.system.AlarmNotificationManagerImpl
import com.mofuapps.selectablenotificationsound.system.NotifyZeroAlarmManagerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SystemModule {

    @Singleton
    @Provides
    fun provideNotifyZeroAlarmManager(@ApplicationContext context: Context): NotifyZeroAlarmManager {
        return NotifyZeroAlarmManagerImpl(context)
    }

    @Singleton
    @Provides
    fun provideAlarmNotificationManager(@ApplicationContext context: Context): AlarmNotificationManager {
        return AlarmNotificationManagerImpl(context)
    }
}