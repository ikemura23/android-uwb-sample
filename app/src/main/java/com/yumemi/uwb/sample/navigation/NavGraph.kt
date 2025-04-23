package com.yumemi.uwb.sample.navigation

sealed class Screen(val route: String) {
    object Select : Screen("select")
    object Controller : Screen("controller")
    object Responder : Screen("responder")
} 