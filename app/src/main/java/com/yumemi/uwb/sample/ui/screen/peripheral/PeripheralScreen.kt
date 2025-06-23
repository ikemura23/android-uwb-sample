package com.yumemi.uwb.sample.ui.screen.peripheral

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.yumemi.uwb.sample.ui.theme.AndroiduwbsampleTheme

@Composable
fun PeripheralScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val viewModel = remember { PeripheralViewModel(
        context = context,
    ) }

    Text("Peripheral Screen", modifier = modifier)
}

@Preview
@Composable
private fun PeripheralScreenPreview() {
    AndroiduwbsampleTheme {
        PeripheralScreen()
    }
}
