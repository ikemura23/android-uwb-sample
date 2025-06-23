package com.yumemi.uwb.sample.navigation

sealed class Screen(val route: String) {
    object Select : Screen("select")
    object Controller : Screen("com/yumemi/uwb/sample/ui/controller")
    object Responder : Screen("responder")
    object Peripheral : Screen("peripheral")
    object BleDeviceConnection : Screen("ble_device_connection")
}
