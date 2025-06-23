package com.yumemi.uwb.sample.ui.screen.bledeviceconnect

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yumemi.uwb.sample.ui.theme.AndroiduwbsampleTheme

@Composable
fun BleDeviceConnectionScreen(
    modifier: Modifier = Modifier,
    onDeviceClick: (String) -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        // 上に2つのBleDeviceItemを配置
        Row(
            modifier = Modifier.weight(1f),
        ) {
            BleDeviceItem(
                modifier = Modifier.weight(1f),
                deviceName = "デバイス1",
                uuId = "1234-5678-9012-3456",
                state = "未接続",
            )
            Spacer(Modifier.size(16.dp))
            BleDeviceItem(
                deviceName = "デバイス2",
                uuId = "2345-6789-0123-4567",
                modifier = Modifier.weight(1f),
                state = "未接続",
            )
        }

        // 下に2つのBleDeviceItemを配置
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            BleDeviceItem(
                deviceName = "デバイス3",
                uuId = "3456-7890-1234-5678",
                modifier = Modifier.weight(1f),
                state = "未接続",
            )
            Spacer(Modifier.size(16.dp))
            BleDeviceItem(
                deviceName = "デバイス4",
                uuId = "4567-8901-2345-6789",
                modifier = Modifier.weight(1f),
                state = "未接続",
            )
        }
    }
}

@Composable
fun BleDeviceItem(
    deviceName: String,
    uuId: String,
    modifier: Modifier = Modifier,
    state: String,
) {
    Column(
        modifier = modifier,
    ) {
        Text(deviceName, color = Color.White)
        Spacer(Modifier.size(16.dp))
        Text(uuId, color = Color.White)
        Spacer(Modifier.size(16.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
            },
        ) {
            Text(state, color = Color.White)
        }
    }
}

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
            deviceName = "デバイス名",
            uuId = "1234-5678-9012-3456",
            state = "未接続",
        )
    }
}
