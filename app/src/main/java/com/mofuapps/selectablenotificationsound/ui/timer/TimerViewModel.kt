package com.mofuapps.selectablenotificationsound.ui.timer

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mofuapps.selectablenotificationsound.domain.alarm.NotifyZeroAlarmManager
import com.mofuapps.selectablenotificationsound.domain.notification.AlarmNotificationManager
import com.mofuapps.selectablenotificationsound.domain.session.CancelSessionUseCase
import com.mofuapps.selectablenotificationsound.domain.session.FinishSessionUseCase
import com.mofuapps.selectablenotificationsound.domain.session.PauseSessionUseCase
import com.mofuapps.selectablenotificationsound.domain.session.ResumeSessionUseCase
import com.mofuapps.selectablenotificationsound.domain.session.Session
import com.mofuapps.selectablenotificationsound.domain.session.SessionRepository
import com.mofuapps.selectablenotificationsound.domain.session.SessionState
import com.mofuapps.selectablenotificationsound.domain.session.StartSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TimerViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    private val startSession: StartSessionUseCase,
    private val pauseSession: PauseSessionUseCase,
    private val resumeSession: ResumeSessionUseCase,
    private val cancelSession: CancelSessionUseCase,
    private val finishSession: FinishSessionUseCase,
    private val alarmManager: NotifyZeroAlarmManager,
    private val notificationManager: AlarmNotificationManager
): ViewModel() {

    private val initialDurationSec = 5

    private val initialUIState = TimerScreenUIState(
        stage = TimerScreenStage.STAND_BY,
        visualIndicator = 1f,
        numericalIndicator = "00:05",
        sound = null,
        soundName = null,
        repeat = false
    )

    private fun Session.textProgress(): String {
        val waitingMillis = (durationMillis() - currentProgressMillis())
        val waiting = waitingMillis / 1000 + if (waitingMillis%1000>0) 1 else 0
        val hours = (waiting / 3600).toInt()
        val residue = (waiting % 3600).toInt()
        val minutes = residue / 60
        val seconds = residue % 60
        val str: String =  if (waiting >= 3600) {
            "%02d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%02d:%02d".format(minutes, seconds)
        }
        return if (str.length <= 8) {
            str
        } else {
            ""
        }
    }

    init {
        sessionRepository.flow.mapLatest { it }.onEach { session ->
            if (session == null) {
                notificationManager.stopNotification()
            }
            session?.let {
                when(it.state) {
                    SessionState.RUNNING -> alarmManager.setAlarm(Date().time + it.remainingMillis(), "HogeHoge", _uiState.value.repeat, _uiState.value.sound)
                    SessionState.PAUSED -> alarmManager.cancelAlarm()
                    SessionState.FINISHED -> {}
                }
            }
        }.launchIn(viewModelScope)

        val tickFlow: Flow<Session?> = sessionRepository.flow.transformLatest { session: Session? ->
            emit(session)
            if (session != null && session.state == SessionState.RUNNING) {
                while(true) {
                    delay(1000L)
                    emit(session)
                }
            }
        }

        tickFlow.onEach { result: Session? ->
            var stage = initialUIState.stage
            var visualIndicator = initialUIState.visualIndicator
            var numericalIndicator = initialUIState.numericalIndicator
            result?.let { session: Session ->
                Log.d("session", session.toString())
                stage = when(session.state) {
                    SessionState.RUNNING -> TimerScreenStage.RUNNING
                    SessionState.PAUSED -> TimerScreenStage.PAUSED
                    SessionState.FINISHED -> TimerScreenStage.FINISHED
                }
                visualIndicator = 1f - (session.progressPercent() / 100).toFloat()
                numericalIndicator = session.textProgress()
            }
            _uiState.update {
                it.copy(
                    stage = stage,
                    visualIndicator = visualIndicator,
                    numericalIndicator = numericalIndicator
                )
            }
            if (result != null && result.state == SessionState.RUNNING && result.remainingMillis() <= 0) {
                finishSession()
            }
        }.launchIn(viewModelScope)
    }

    private val _uiState = MutableStateFlow (
        initialUIState
    )

    internal val uiState = _uiState.asStateFlow()

    internal fun setSound(sound: Uri?, soundName: String?) {
        _uiState.update {
            it.copy(sound = sound, soundName = soundName)
        }
    }

    internal fun updateRepeat(repeat: Boolean) {
        _uiState.update {
            it.copy(repeat = repeat)
        }
    }

    internal fun startTimer() {
        alarmManager.setAlarm(Date().time + initialDurationSec * 1000L, "A", _uiState.value.repeat, _uiState.value.sound)
        viewModelScope.launch {
            startSession(initialDurationSec)
        }
    }

    internal fun pauseTimer() {
        alarmManager.cancelAlarm()
        viewModelScope.launch {
            pauseSession()
        }
    }

    internal fun resumeTimer() {
        viewModelScope.launch {
            resumeSession()
        }
    }

    internal fun cancelTimer() {
        viewModelScope.launch {
            cancelSession()
        }
    }
}