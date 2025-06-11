package com.yumemi.uwb.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yumemi.uwb.sample.navigation.Screen
import com.yumemi.uwb.sample.ui.controller.ControllerScreen
import com.yumemi.uwb.sample.ui.responder.ResponderScreen
import com.yumemi.uwb.sample.ui.home.SelectScreen
import com.yumemi.uwb.sample.ui.theme.AndroiduwbsampleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroiduwbsampleTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Select.route,
        modifier = modifier,
    ) {
        composable(Screen.Select.route) {
            SelectScreen(
                onControllerClick = { navController.navigate(Screen.Controller.route) },
                onResponderClick = { navController.navigate(Screen.Responder.route) },
            )
        }
        composable(Screen.Controller.route) {
            ControllerScreen()
        }
        composable(Screen.Responder.route) {
            ResponderScreen()
        }
    }
}
