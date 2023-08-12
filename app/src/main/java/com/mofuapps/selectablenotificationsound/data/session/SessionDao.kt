package com.mofuapps.selectablenotificationsound.data.session

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Transaction
    @Query("SELECT * FROM session WHERE id=0 LIMIT 1")
    fun findFlow(): Flow<SessionEntity?>

    @Transaction
    @Query("SELECT * FROM session WHERE id=0 LIMIT 1")
    suspend fun find(): SessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SessionEntity)

    @Update
    suspend fun update(entity: SessionEntity)

    @Delete
    suspend fun delete(entity: SessionEntity)
}
