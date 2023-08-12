package com.mofuapps.selectablenotificationsound.domain.session

import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResumeSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke() {
        val currentSession = sessionRepository.find()
        currentSession?.let {
            val updatedSession = currentSession.copy(
                resumedAt = Date(),
                state = SessionState.RUNNING
            )
            sessionRepository.update(updatedSession)
        }
    }
}
