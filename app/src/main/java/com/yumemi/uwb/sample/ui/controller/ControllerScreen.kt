package com.yumemi.uwb.sample.ui.controller

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.uwb.RangingPosition
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yumemi.uwb.sample.ui.components.UwbContent
import com.yumemi.uwb.sample.ui.theme.AndroiduwbsampleTheme
import com.yumemi.uwb.sample.uwb.UwbController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControllerScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val uwbController = UwbController(context)
    val uwbPosition: MutableState<RangingPosition?> = remember { mutableStateOf(null) }
    val viewModel: ControllerViewModel = viewModel()

    LaunchedEffect(Unit) {
        viewModel.initializeWifiAware(context)
        uwbController.startRanging()
        uwbController.rangingPosition.collect { position ->
            uwbPosition.value = position
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            uwbController.cancelRanging()
            viewModel.cancelWifiAware()
        }
    }
    Scaffold(
        modifier = modifier.background(color = Color.Companion.Gray),
        topBar = {
            TopAppBar(
                title = { Text(text = "コントローラー") },
                modifier = Modifier.Companion.padding(start = 58.dp),
            )
        },
    ) { innerPadding ->
        UwbContent(
            modifier = Modifier.Companion.padding(innerPadding),
            distance = uwbPosition.value?.distance?.value,
            content = {
                Button(
                    onClick = {
                        viewModel.toggleAutoSending()
                    },
                ) {
                    Text("Wifi Aware開始")
                }
            },
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = 1280,
    heightDp = 720,
)
@Composable
fun ControllerScreenPreview() {
    AndroiduwbsampleTheme {
        ControllerScreen()
    }
}
