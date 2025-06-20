package com.yumemi.uwb.sample.ui

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
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
            BleDeviceItem(Modifier.weight(1f))
            Spacer(Modifier.size(16.dp))
            BleDeviceItem(Modifier.weight(1f))
        }

        // 下に2つのBleDeviceItemを配置
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
        ) {
            BleDeviceItem(Modifier.weight(1f))
            Spacer(Modifier.size(16.dp))
            BleDeviceItem(Modifier.weight(1f))
        }
    }
}

@Composable
fun BleDeviceItem(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
    ) {
        Text("デバイス1", color = Color.White)
        Spacer(Modifier.size(16.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {

            },
        ) {
            Text("デバイス1", color = Color.White)
        }
    }

    // Box(
    //     modifier = modifier
    //         .fillMaxSize()
    //         .background(color = Color(0xFF777777))
    //         .clickable {
    //             // TODO: Handle click event for BLE device item
    //         },
    // ) {
    //     Column {
    //         Text("デバイス1", color = Color.White)
    //         Text("デバイス2", color = Color.White)
    //     }
    // }
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
        BleDeviceItem()
    }
}
