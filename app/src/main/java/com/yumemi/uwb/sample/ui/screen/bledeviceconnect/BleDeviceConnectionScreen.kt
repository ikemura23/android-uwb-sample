package com.yumemi.uwb.sample.ui.screen.bledeviceconnect

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
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
    val devices = uiState.devices.values.sortedBy { device ->
        DeviceType.ALL.indexOfFirst { it.uuid == device.id }
    }

    Scaffold(
        modifier = modifier.background(color = Color.Black),
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text(text = "4台 デバイス接続") },
                modifier = Modifier.padding(start = 58.dp),
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.startRanging()
                },
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "開始")
            }
        },
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(start = 58.dp, top = 16.dp, bottom = 16.dp, end = 16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.SpaceAround,
            ) {
                devices.forEach { device: BleDevice ->
                    BleContent(
                        onClick = { viewModel.onDeviceClick(context, device.id) },
                        bleStatus = when {
                            device.isLoading -> "接続中..."
                            device.isBleConnected -> "接続済み"
                            else -> "未接続"
                        },
                        name = device.name,
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.SpaceAround,
            ) {
                devices.forEach { device ->
                    Text("TODO: UWBのstatus", color = Color.White)
                }
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
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onClick,
        ) {
            Text(
                if (device.isBleConnected) "切断" else "接続",
                color = Color.White,
            )
        }
    }
}

@Composable
fun BleContent(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    bleStatus: String = "未接続",
    name: String = "デバイス名",
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = CenterVertically,
    ) {
        Button(
            onClick = onClick,
        ) {
            Text(name)
        }
        Spacer(modifier = Modifier.size(16.dp))
        Text(
            bleStatus,
            color = Color.White,
        )
    }
}

// プレビュー用のダミーデータ
private val previewDevices = listOf(
    BleDevice(
        id = DeviceType.GREEN.uuid,
        name = "デバイス1",
    ),
    BleDevice(
        id = DeviceType.RED.uuid,
        name = "デバイス2",
    ),
    BleDevice(
        id = DeviceType.YELLOW.uuid,
        name = "デバイス3",
    ),
    BleDevice(
        id = DeviceType.BROWN.uuid,
        name = "デバイス4",
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

@Preview
@Composable
private fun BleContentPreview() {
    AndroiduwbsampleTheme {
        BleContent(
            onClick = {},
        )
    }
}
