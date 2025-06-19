package com.yumemi.uwb.sample.ui.responder

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.uwb.RangingPosition
import com.yumemi.uwb.sample.ui.components.UwbContent
import com.yumemi.uwb.sample.ui.theme.AndroiduwbsampleTheme
import com.yumemi.uwb.sample.uwb.UwbResponder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponderScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    // controller の位置
    val uwbPosition = remember { mutableStateOf<RangingPosition?>(null) }
    val uwbResponder = remember { UwbResponder(context) }

    LaunchedEffect(Unit) {
        uwbResponder.rangingResult.collect { position ->
            uwbPosition.value = position
        }

        try {
            uwbResponder.startRanging()
        } catch (e: Exception) {
            Log.e("ResponderScreen", "Failed to start ranging", e)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            uwbResponder.cancelRanging()
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
