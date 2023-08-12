package com.mofuapps.selectablenotificationsound.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mofuapps.selectablenotificationsound.data.session.SessionDao
import com.mofuapps.selectablenotificationsound.data.session.SessionEntity

@Database(
    entities = [
        SessionEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase: RoomDatabase() {
    abstract val sessionDao: SessionDao
}

private lateinit var INSTANCE: AppDatabase

fun getDatabase(context: Context): AppDatabase {
    synchronized(AppDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext,
                AppDatabase::class.java,
                "app_database").build()
        }
    }
    return INSTANCE
}
