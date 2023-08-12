package com.mofuapps.selectablenotificationsound.domain.session

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import java.util.Date

class FinishSessionUseCaseTest {
    private val sessionRepository = object: SessionRepository {
        private val dataSource = MutableSharedFlow<Session?>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )

        override val flow: Flow<Session?>
            get() = dataSource

        init {
            val now = Date()
            dataSource.tryEmit(
                Session(
                    durationSec = 1,
                    progressMillisAtResumed = 0,
                    resumedAt = now,
                    state = SessionState.PAUSED
                )
            )
        }

        override suspend fun find(): Session? {
            return dataSource.firstOrNull()
        }

        override suspend fun insert(session: Session) {
            dataSource.emit(session)
        }

        override suspend fun update(session: Session) {
            dataSource.emit(session)
        }
        override suspend fun delete(session: Session) {
            dataSource.emit(null)
        }
    }

    @Test fun finishSessionTest() = runTest {
        val finishSession = FinishSessionUseCase(sessionRepository)
        sessionRepository.find()
        finishSession()
        val result = sessionRepository.find()
        assertNotNull(result)
        result?.let {
            assertEquals(SessionState.FINISHED, it.state)
        }
    }
}