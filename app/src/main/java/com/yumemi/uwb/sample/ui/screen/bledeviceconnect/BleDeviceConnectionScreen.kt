package com.yumemi.uwb.sample.ui.screen.bledeviceconnect

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yumemi.uwb.sample.ui.theme.AndroiduwbsampleTheme

/**
 * BLEデバイス接続画面 セントラル：親機
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BleDeviceConnectionScreen(
    modifier: Modifier = Modifier,
    viewModel: BleDeviceConnectionViewModel = viewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = "BLEデバイス接続") },
                modifier = Modifier.padding(start = 58.dp),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
        ) {
            // デバイスリスト表示
            val devices = uiState.devices

            // 上に2つのBleDeviceItemを配置
            Row(
                modifier = Modifier.weight(1f),
            ) {
                BleDeviceItem(
                    modifier = Modifier.weight(1f),
                    device = devices.getOrNull(0) ?: BleDevice("", "", ""),
                    onClick = { viewModel.onDevice1Click(context) },
                )
                Spacer(Modifier.size(16.dp))
                BleDeviceItem(
                    device = devices.getOrNull(1) ?: BleDevice("", "", ""),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onDevice2Click(context) },
                )
            }

            // 下に2つのBleDeviceItemを配置
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                BleDeviceItem(
                    device = devices.getOrNull(2) ?: BleDevice("", "", ""),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onDevice3Click(context) },
                )
                Spacer(Modifier.size(16.dp))
                BleDeviceItem(
                    device = devices.getOrNull(3) ?: BleDevice("", "", ""),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onDevice4Click(context) },
                )
            }

            // エラーメッセージ表示
            uiState.errorMessage?.let { errorMessage ->
                Spacer(Modifier.size(16.dp))
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
fun BleDeviceItem(
    device: BleDevice,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Column(
        modifier = modifier,
    ) {
        Text(device.name, color = Color.White)
        Spacer(Modifier.size(16.dp))
        Text(device.uuid, color = Color.White)
        Spacer(Modifier.size(16.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onClick,
        ) {
            Text(
                if (device.isConnected) "切断" else "接続",
                color = Color.White,
            )
        }
    }
}

// プレビュー用のダミーデータ
private val previewDevices = listOf(
    BleDevice(
        id = "device1",
        name = "デバイス1",
        uuid = "1234-5678-9012-3456",
        isConnected = false,
    ),
    BleDevice(
        id = "device2",
        name = "デバイス2",
        uuid = "2345-6789-0123-4567",
        isConnected = true,
    ),
    BleDevice(
        id = "device3",
        name = "デバイス3",
        uuid = "3456-7890-1234-5678",
        isConnected = false,
    ),
    BleDevice(
        id = "device4",
        name = "デバイス4",
        uuid = "4567-8901-2345-6789",
        isConnected = false,
    ),
)

@Preview(device = "spec:width=720dp,height=360dp")
@Composable
private fun BleDeviceConnectionScreenLandscapePreview() {
    AndroiduwbsampleTheme {
        BleDeviceConnectionScreen()
    }
}

@Preview
@Composable
private fun BleDeviceItemPreview() {
    AndroiduwbsampleTheme {
        BleDeviceItem(
            device = previewDevices[0],
        )
    }
}
