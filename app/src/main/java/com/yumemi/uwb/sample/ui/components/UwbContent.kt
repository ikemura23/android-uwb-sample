package com.yumemi.uwb.sample.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yumemi.uwb.sample.ui.theme.AndroiduwbsampleTheme

@Composable
fun UwbContent(
    modifier: Modifier = Modifier,
    distance: String,
) {
    Box(
        modifier = modifier.width(200.dp),
    ) {
        Row(
        ) {
            Text(
                text = "距離 = ",
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "$distance m",
            )
        }

    }
}

@Preview(showBackground = true)
@Composable
private fun UwbContentPreview() {
    AndroiduwbsampleTheme {
        UwbContent(distance = "1234123.4")
    }
}
