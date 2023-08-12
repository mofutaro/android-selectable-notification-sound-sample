package com.mofuapps.selectablenotificationsound.ui.timer

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mofuapps.selectablenotificationsound.R
import com.mofuapps.selectablenotificationsound.ui.SoundActivity
import com.mofuapps.selectablenotificationsound.ui.theme.BGCountdownTimerTheme
import xyz.aprildown.ultimateringtonepicker.RingtonePickerActivity
import xyz.aprildown.ultimateringtonepicker.UltimateRingtonePicker

@Composable
fun TimerScreen(modifier: Modifier = Modifier, timerViewModel: TimerViewModel = hiltViewModel()) {
    val uiState by timerViewModel.uiState.collectAsState()

    TimerScreen(
        uiState = uiState,
        onStartClicked = remember { { timerViewModel.startTimer() } },
        onPauseClicked = remember { { timerViewModel.pauseTimer() } },
        onResumeClicked = remember { { timerViewModel.resumeTimer() } },
        onCancelClicked = remember { { timerViewModel.cancelTimer() } },
        onSoundSelected = remember { { timerViewModel.setSound(it?.uri, it?.name) } },
        onCheckChanged = remember { { timerViewModel.updateRepeat(it) } },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TimerScreen(
    uiState: TimerScreenUIState,
    onStartClicked: () -> Unit,
    onPauseClicked: () -> Unit,
    onResumeClicked: () -> Unit,
    onCancelClicked: () -> Unit,
    onSoundSelected: (UltimateRingtonePicker.RingtoneEntry?) -> Unit,
    onCheckChanged: (repeat:Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val ringtoneLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK && it.data != null) {
            // The id of the channel.
            onSoundSelected(RingtonePickerActivity.getPickerResult(it.data).firstOrNull())
        }
    }
    Surface(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("SelectableNotificationSound", fontSize = 30.sp, fontWeight = FontWeight.W300)


            Spacer(modifier = Modifier.height(20.dp))
            NumericalIndicator(indicator = uiState.numericalIndicator, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(64.dp))
            Row(modifier = Modifier.padding(horizontal = 20.dp)) {
                val buttonWidth = 150.dp
                OutlinedButton(
                    onClick = onCancelClicked,
                    enabled = uiState.stage != TimerScreenStage.STAND_BY,
                    modifier = Modifier.width(buttonWidth)
                ) {
                    Text("キャンセル")
                }
                Spacer(modifier = Modifier.weight(1f))
                var rightButtonLabel by remember { mutableStateOf("") }
                var rightButtonEnabled by remember { mutableStateOf(true) }
                var onRightButtonClicked by remember { mutableStateOf({}) }
                LaunchedEffect(uiState.stage) {
                    when (uiState.stage) {
                        TimerScreenStage.STAND_BY -> {
                            onRightButtonClicked = onStartClicked
                            rightButtonEnabled = true
                            rightButtonLabel = "開始"
                        }
                        TimerScreenStage.RUNNING -> {
                            onRightButtonClicked = onPauseClicked
                            rightButtonEnabled = true
                            rightButtonLabel = "一時停止"
                        }
                        TimerScreenStage.PAUSED -> {
                            onRightButtonClicked = onResumeClicked
                            rightButtonEnabled = true
                            rightButtonLabel = "再開"
                        }
                        TimerScreenStage.FINISHED -> {
                            onRightButtonClicked = onStartClicked
                            rightButtonEnabled = false
                            rightButtonLabel = "開始"
                        }
                    }
                }
                OutlinedButton(
                    onClick = onRightButtonClicked,
                    enabled = rightButtonEnabled,
                    modifier = Modifier.width(buttonWidth)
                ) {
                    Text(rightButtonLabel)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            ListItem(
                modifier = Modifier
                    .height(64.dp)
                    .clickable {
                        ringtoneLauncher.launch(
                            Intent(
                                context,
                                SoundActivity::class.java
                            )
                        )
                    },
                trailing = {
                    Box(modifier = Modifier.padding(end = 12.dp)) {
                        if (uiState.soundName != null) {
                            Text(uiState.soundName, style = MaterialTheme.typography.body1)
                        } else {
                            Icon(painter = painterResource(id = R.drawable.baseline_arrow_forward_ios_24), contentDescription = null)
                        }
                    }

                }
            ) {
                Text("通知音を選択する")
            }
            ListItem(
                modifier = Modifier.height(64.dp),
                trailing = {
                    Checkbox(checked = uiState.repeat, onCheckedChange = onCheckChanged)
                }
            ) {
                Text("通知を繰り返す")
            }
        }
    }
}

private class TimerScreenUIStateProvider: PreviewParameterProvider<TimerScreenUIState> {
    override val values: Sequence<TimerScreenUIState> = sequenceOf(
        TimerScreenUIState(
            stage = TimerScreenStage.STAND_BY,
            visualIndicator = 1f,
            numericalIndicator = "05:00",
            sound = null,
            soundName = null,
            false
        ),
        TimerScreenUIState(
            stage = TimerScreenStage.RUNNING,
            visualIndicator = 0.5f,
            numericalIndicator = "02:30",
            sound = null,
            soundName = null,
            repeat = true
        ),
        TimerScreenUIState(
            stage = TimerScreenStage.PAUSED,
            visualIndicator = 0.5f,
            numericalIndicator = "02:30",
            sound = null,
            soundName = null,
            repeat = false
        ),
        TimerScreenUIState(
            stage = TimerScreenStage.FINISHED,
            visualIndicator = 0f,
            numericalIndicator = "00:00",
            sound = null,
            soundName = null,
            repeat = true
        )
    )
}

@Preview
@Composable
private fun TimerScreenPreview(
    @PreviewParameter(provider = TimerScreenUIStateProvider::class) data: TimerScreenUIState
) {
    BGCountdownTimerTheme {
        TimerScreen(
            uiState = data,
            onStartClicked = {},
            onPauseClicked = {},
            onResumeClicked = {},
            onCancelClicked = {},
            onSoundSelected = {},
            onCheckChanged = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}
