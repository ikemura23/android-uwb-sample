package com.yumemi.uwb.sample.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yumemi.uwb.sample.ui.theme.AndroiduwbsampleTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectScreen(
    modifier: Modifier = Modifier,
    onControllerClick: () -> Unit = {},
    onResponderClick: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = "UWB サンプル アプリ") },
                modifier = Modifier.padding(start = 58.dp),
            )

        },
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
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
}

@Preview(
    showBackground = true,
    widthDp = 1280,
    heightDp = 720,
)
@Composable
private fun SelectScreenPreview() {
    AndroiduwbsampleTheme {
        SelectScreen()
    }
}
