package com.mofuapps.selectablenotificationsound.ui.timer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.mofuapps.selectablenotificationsound.ui.theme.BGCountdownTimerTheme

@Composable
fun NumericalIndicator(indicator: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.Center) {
        val density = LocalDensity.current
        Text(
            text = indicator,
            color = MaterialTheme.colors.onBackground,
            fontSize = with(density) { 50.dp.toSp() }
        )
    }
}

private class NumericalIndicatorValueProvider: PreviewParameterProvider<String> {
    override val values: Sequence<String> = sequenceOf(
        "00:00",
        "10:55",
        "1:00:00"
    )
}

@Preview
@Composable
private fun NumericalIndicatorPreview(
    @PreviewParameter(provider = NumericalIndicatorValueProvider::class) data: String
) {
    BGCountdownTimerTheme {
        NumericalIndicator(indicator = data)
    }
}