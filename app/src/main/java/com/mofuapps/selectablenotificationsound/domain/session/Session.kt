package com.mofuapps.selectablenotificationsound.domain.session

import java.util.Date
import kotlin.math.max


data class Session(
    val durationSec: Int,
    val progressMillisAtResumed: Long,
    val resumedAt: Date,
    val state: SessionState
) {
    private fun isRunning(): Boolean {
        return state == SessionState.RUNNING
    }

    fun durationMillis(): Long {
        return durationSec.toLong() * 1000L
    }

    private fun millisUntil(date: Date): Long {
        return date.time - resumedAt.time
    }

    fun currentProgressMillis(): Long {
        return if (isRunning())
            progressMillisAtResumed + millisUntil(Date())
        else
            progressMillisAtResumed
    }

    fun remainingMillis(): Long {
        return durationMillis() - currentProgressMillis()
    }

    fun progressPercent(): Double {
        val percent: Double = currentProgressMillis() / durationMillis().toDouble()
        return max(percent, 1.0) * 100L
    }
}
