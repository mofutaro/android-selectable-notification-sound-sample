package com.mofuapps.selectablenotificationsound.domain.session

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.assertNotNull

class StartSessionUseCaseTest {
    private val sessionRepository = object: SessionRepository {
        private val dataSource = MutableSharedFlow<Session>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )

        override val flow: Flow<Session>
            get() = dataSource

        override suspend fun find(): Session? { return dataSource.firstOrNull() }
        override suspend fun insert(session: Session) {
            dataSource.emit(session)
        }
        override suspend fun update(session: Session) {}
        override suspend fun delete(session: Session) {}
    }

    @Test fun startSessionTest() = runTest {
        val startSession = StartSessionUseCase(sessionRepository)
        startSession(1)
        val addedSession: Session? = sessionRepository.find()
        assertNotNull(addedSession)
    }
}