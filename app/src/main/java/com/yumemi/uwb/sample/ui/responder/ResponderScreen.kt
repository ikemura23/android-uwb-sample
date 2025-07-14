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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yumemi.uwb.sample.ui.components.UwbContent
import com.yumemi.uwb.sample.ui.theme.AndroiduwbsampleTheme
import com.yumemi.uwb.sample.uwb.UwbResponder
import com.yumemi.uwb.sample.wifiaware.WifiAwareManagerWrapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponderScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val viewModel = remember {
        ResponderViewModel(
            UwbResponder(context),
        )
    }
    LaunchedEffect(Unit) {
        // Wifi Aware の初期化や接続処理が必要な場合はここで行う
        val wifiAwareManager = WifiAwareManagerWrapper(
            context = context,
            onMessageReceived = { peerHandle, message ->
                // メッセージ受信時の処理
                Log.d("ControllerScreen", "Message received from peer ${peerHandle}: $message")
            },
            onAwareUnavailable = {
                Log.d("ControllerScreen", "onAwareUnavailable")
            },
        )
        wifiAwareManager.initialize()
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
            distance = viewModel.uwbPosition.value?.distance?.value,
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
