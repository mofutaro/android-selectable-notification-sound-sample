package com.mofuapps.selectablenotificationsound.domain.session

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.assertNull
import java.util.Date

class CancelSessionUseCaseTest {
    private val sessionRepository = object: SessionRepository {
        private val dataSource = MutableSharedFlow<Session?>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )

        override val flow: Flow<Session?>
            get() = dataSource

        init {
            dataSource.tryEmit(
                Session(
                    durationSec = 1,
                    progressMillisAtResumed = 0,
                    resumedAt = Date(),
                    state = SessionState.RUNNING
                )
            )
        }

        override suspend fun find(): Session? { return dataSource.firstOrNull() }
        override suspend fun insert(session: Session) {
            dataSource.emit(session)
        }
        override suspend fun update(session: Session) {}
        override suspend fun delete(session: Session) {
            dataSource.emit(null)
        }
    }
    @Test fun cancelSessionTest() = runTest {
        val cancelSession = CancelSessionUseCase(sessionRepository)
        cancelSession()
        val result = sessionRepository.find()
        assertNull(result)
    }
}