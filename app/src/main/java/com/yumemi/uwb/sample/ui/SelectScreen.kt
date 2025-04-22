package com.yumemi.uwb.sample.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yumemi.uwb.sample.ui.theme.AndroiduwbsampleTheme

@Composable
fun SelectScreen(
    modifier: Modifier = Modifier,
    onControllerClick: () -> Unit = {},
    onResponderClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(onClick = onControllerClick) {
            Text("コントローラー")
        }
        Spacer(modifier = Modifier.padding(16.dp))
        Button(onClick = onResponderClick) {
            Text("レスポンダー")
        }
    }
}

@Preview
@Composable
private fun SelectScreenPreview() {
    AndroiduwbsampleTheme {
        SelectScreen()
    }
}
