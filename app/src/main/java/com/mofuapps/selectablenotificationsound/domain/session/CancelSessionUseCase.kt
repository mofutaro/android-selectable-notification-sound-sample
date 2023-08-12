package com.mofuapps.selectablenotificationsound.domain.session

import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class CancelSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke() {
        val currentSession: Session? = sessionRepository.find()
        currentSession?.let {
            sessionRepository.delete(it)
        }
    }
}