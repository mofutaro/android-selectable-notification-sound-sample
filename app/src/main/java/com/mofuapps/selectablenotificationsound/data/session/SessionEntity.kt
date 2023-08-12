package com.mofuapps.selectablenotificationsound.data.session

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mofuapps.selectablenotificationsound.domain.session.Session
import com.mofuapps.selectablenotificationsound.domain.session.SessionState
import java.util.Date

@Entity(tableName = "session")
data class SessionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,
    val durationSec: Int,
    val progressMillisAtResumed: Long,
    val resumedAt: Date,
    val state: SessionState
)

fun SessionEntity.asDomainModel(): Session {
    return Session(
        durationSec = durationSec,
        progressMillisAtResumed = progressMillisAtResumed,
        resumedAt = resumedAt,
        state = state
    )
}

fun Session.asDatabaseModel(): SessionEntity {
    return SessionEntity(
        id = 0,
        durationSec = durationSec,
        progressMillisAtResumed = progressMillisAtResumed,
        resumedAt = resumedAt,
        state = state
    )
}
