package com.yumemi.uwb.sample.ui.responder

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.uwb.RangingParameters
import androidx.core.uwb.RangingPosition
import androidx.core.uwb.RangingResult
import androidx.core.uwb.UwbComplexChannel
import androidx.core.uwb.UwbDevice
import androidx.core.uwb.UwbManager
import com.yumemi.uwb.sample.oob.ble.BleCentral
import com.yumemi.uwb.sample.ui.components.UwbContent
import com.yumemi.uwb.sample.ui.theme.AndroiduwbsampleTheme
import com.yumemi.uwb.sample.uwb.UwbControllerParams
import com.yumemi.uwb.sample.uwb.logValue
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponderScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    // controller の位置
    val uwbPosition = remember { mutableStateOf<RangingPosition?>(null) }

    LaunchedEffect(Unit) {
        val uwbManager = UwbManager.createInstance(context)
        val controleeSession = uwbManager.controleeSessionScope()

        // controller に送る
        val addressByteArray = controleeSession.localAddress.address

        // BLE GATT サーバーへ接続し、UWB ホストと接続に必要なパラメーターを送受信する
        val bleCentral = BleCentral(context)
        bleCentral.connectGattServer()
        val uwbControllerParamsByteArray = bleCentral.readCharacteristic()
        val uwbControllerParams = UwbControllerParams.decode(uwbControllerParamsByteArray)
        bleCentral.writeCharacteristic(addressByteArray)
        bleCentral.destroy()

        // RangingParameters を作り UWB 接続を開始する
        val rangingParameters = RangingParameters(
            uwbConfigType = RangingParameters.CONFIG_MULTICAST_DS_TWR,
            complexChannel = UwbComplexChannel(uwbControllerParams.channel, uwbControllerParams.preambleIndex),
            peerDevices = listOf(UwbDevice.createForAddress(uwbControllerParams.address)),
            updateRateType = RangingParameters.RANGING_UPDATE_RATE_AUTOMATIC,
            sessionId = uwbControllerParams.sessionId,
            sessionKeyInfo = uwbControllerParams.sessionKeyInfo,
            subSessionId = 0, // SESSION_ID_UNSET ？
            subSessionKeyInfo = null, // ？
        )

        launch {
            controleeSession.prepareSession(rangingParameters).collect { rangingResult ->
                when (rangingResult) {
                    is RangingResult.RangingResultPosition -> {

                        Log.d("UwbResponder", "device: ${rangingResult.device.address}, position: ${rangingResult.position.logValue()}")
                        uwbPosition.value = rangingResult.position
                    }

                    is RangingResult.RangingResultPeerDisconnected -> {
                        uwbPosition.value = null
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = "レスポンダー") },
                modifier = Modifier.padding(start = 58.dp),
            )
        },
    ) { innerPadding ->
        UwbContent(
            modifier = Modifier
                .padding(innerPadding)
                .background(color = Color.DarkGray),
            distance = uwbPosition.value?.distance?.value,
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = 1280,
    heightDp = 720,
)
@Composable
fun ResponderScreenPreview() {
    AndroiduwbsampleTheme {
        ResponderScreen()
    }
}
