package com.yumemi.uwb.sample.ui.screen.peripheral

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yumemi.uwb.sample.ui.components.UwbContent
import com.yumemi.uwb.sample.ui.theme.AndroiduwbsampleTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeripheralScreen(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val viewModel = remember {
        PeripheralViewModel(
            context = context,
        )
    }

    val rangingPosition by viewModel.rangingPosition.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = "ペリフェラル") },
                modifier = Modifier.padding(start = 58.dp),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // UWB測距開始ボタン
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "UWB測距制御",
                    fontSize = 20.sp,
                    color = Color.White,
                )
                Button(
                    onClick = {
                        viewModel.startUwb()
                    },
                ) {
                    Text("Start UWB")
                }
            }

            Spacer(modifier = Modifier.size(16.dp))

            // 距離表示
            UwbContent(
                modifier = Modifier.weight(1f),
                distance = rangingPosition?.position?.distance?.value,
            )

            // 詳細情報表示
            if (rangingPosition != null) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "詳細情報:",
                        fontSize = 18.sp,
                        color = Color.White,
                    )
                    Text(
                        text = "方位角: ${rangingPosition?.position?.azimuth?.value?.let { "%.2f°".format(it) } ?: "N/A"}",
                        fontSize = 16.sp,
                        color = Color.Cyan,
                    )
                    Text(
                        text = "仰角: ${rangingPosition?.position?.elevation?.value?.let { "%.2f°".format(it) } ?: "N/A"}",
                        fontSize = 16.sp,
                        color = Color.Cyan,
                    )
                    Text(
                        text = "タイムスタンプ: ${rangingPosition?.position?.elapsedRealtimeNanos ?: "N/A"}",
                        fontSize = 14.sp,
                        color = Color.Gray,
                    )
                }
            } else {
                Text(
                    text = "測距データがありません",
                    fontSize = 16.sp,
                    color = Color.Gray,
                )
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
private fun PeripheralScreenPreview() {
    AndroiduwbsampleTheme {
        PeripheralScreen()
    }
}
