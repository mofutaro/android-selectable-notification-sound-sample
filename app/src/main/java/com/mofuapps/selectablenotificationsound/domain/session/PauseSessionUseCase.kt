package com.mofuapps.selectablenotificationsound.domain.session

import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class PauseSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke() {
        val currentSession = sessionRepository.find()
        currentSession?.let {
            val updatedSession = it.copy(
                progressMillisAtResumed = it.currentProgressMillis(),
                state = SessionState.PAUSED
            )
            sessionRepository.update(updatedSession)
        }
    }
}