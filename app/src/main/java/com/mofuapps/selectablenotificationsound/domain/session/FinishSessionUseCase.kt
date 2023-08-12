package com.mofuapps.selectablenotificationsound.domain.session

import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class FinishSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke() {
        val currentSession: Session? = sessionRepository.find()
        currentSession?.let {
            val updatedSession = it.copy(
                progressMillisAtResumed = it.durationMillis(),
                state = SessionState.FINISHED
            )
            sessionRepository.update(updatedSession)
        }
    }
}