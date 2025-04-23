package com.yumemi.uwb.sample.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yumemi.uwb.sample.ui.components.UwbContent
import com.yumemi.uwb.sample.ui.theme.AndroiduwbsampleTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponderScreen(modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text(text = "レスポンダー") })
        },
    ) { innerPadding ->
        UwbContent(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            distance = "dummy distance",
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ResponderScreenPreview() {
    AndroiduwbsampleTheme {
        ResponderScreen()
    }
}
