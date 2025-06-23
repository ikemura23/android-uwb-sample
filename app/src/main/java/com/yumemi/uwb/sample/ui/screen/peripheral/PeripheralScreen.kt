package com.yumemi.uwb.sample.ui.screen.peripheral

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yumemi.uwb.sample.ui.theme.AndroiduwbsampleTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeripheralScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val viewModel = remember { PeripheralViewModel(
        context = context,
    ) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = "ペリフェラル") },
                modifier = Modifier.padding(start = 58.dp),
            )
        },
    ) { innerPadding ->
        Text(
            "Peripheral Screen", 
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Preview
@Composable
private fun PeripheralScreenPreview() {
    AndroiduwbsampleTheme {
        PeripheralScreen()
    }
}
