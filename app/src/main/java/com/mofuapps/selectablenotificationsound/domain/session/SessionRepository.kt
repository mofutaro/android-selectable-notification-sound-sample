package com.mofuapps.selectablenotificationsound.domain.session

import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    val flow: Flow<Session?>
    suspend fun find(): Session?
    suspend fun insert(session: Session)
    suspend fun update(session: Session)
    suspend fun delete(session: Session)
}