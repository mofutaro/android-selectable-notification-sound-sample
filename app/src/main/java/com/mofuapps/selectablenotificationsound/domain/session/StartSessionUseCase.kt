package com.mofuapps.selectablenotificationsound.domain.session

import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StartSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(durationSec: Int) {
        val newSession = Session(
            durationSec = durationSec,
            progressMillisAtResumed = 0,
            resumedAt = Date(),
            state = SessionState.RUNNING
        )
        sessionRepository.insert(newSession)
    }
}