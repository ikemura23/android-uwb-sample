package com.yumemi.uwb.sample.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yumemi.uwb.sample.ui.theme.AndroiduwbsampleTheme
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun UwbContent(
    modifier: Modifier = Modifier,
    distance: Float?,
    content: @Composable () -> Unit = { /* No-op */ },
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 74.dp, end = 56.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            // 距離
            Text(
                text = "距離(m)",
                fontSize = 60.sp,
            )
            Spacer(modifier = Modifier.padding(24.dp))
            content()
            Spacer(modifier = Modifier.weight(1f))
            // 値
            Text(
                text = distance?.let(::formatDistance) ?: "",
                color = Color.Green,
                fontSize = 240.sp,
            )
        }
    }
}

private fun formatDistance(distance: Float): String {
    val normalizedDistance = if (distance < 0) 0f else distance
    return BigDecimal(normalizedDistance.toString())
        .setScale(1, RoundingMode.HALF_UP)
        .toString()
}

@Preview(
    showBackground = true,
    widthDp = 1280,
    heightDp = 720,
)
@Composable
private fun UwbContentPreview() {
    AndroiduwbsampleTheme {
        Column {
            UwbContent(distance = 20.4F)
            UwbContent(distance = 100.45F)
            UwbContent(distance = 22.44F)
            UwbContent(distance = -1.5F)
            UwbContent(distance = null)
        }
    }
}
