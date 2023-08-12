package com.mofuapps.selectablenotificationsound.data.session

import com.mofuapps.selectablenotificationsound.di.IoDispatcher
import com.mofuapps.selectablenotificationsound.domain.session.Session
import com.mofuapps.selectablenotificationsound.domain.session.SessionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealSessionRepository @Inject constructor(
    private val sessionDao: SessionDao,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
    ): SessionRepository {
    override val flow: Flow<Session?> = sessionDao.findFlow().transform {
        emit(it?.asDomainModel())
    }.flowOn(dispatcher)

    override suspend fun find(): Session? = withContext(dispatcher) {
        sessionDao.find()?.asDomainModel()
    }

    override suspend fun insert(session: Session) = withContext(dispatcher) {
        sessionDao.insert(session.asDatabaseModel())
    }

    override suspend fun update(session: Session) = withContext(dispatcher) {
        sessionDao.update(session.asDatabaseModel())
    }

    override suspend fun delete(session: Session) = withContext(dispatcher) {
        sessionDao.delete(session.asDatabaseModel())
    }
}