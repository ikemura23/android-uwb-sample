package com.yumemi.uwb.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.yumemi.uwb.sample.ui.SelectScreen
import com.yumemi.uwb.sample.ui.theme.AndroiduwbsampleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroiduwbsampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SelectScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
