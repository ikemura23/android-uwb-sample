package com.yumemi.uwb.sample.wifiaware

import android.net.wifi.aware.PeerHandle

sealed class WifiAwareState {
    // 切断
    data object Disconnected : WifiAwareState()

    // 接続中
    data object Connecting : WifiAwareState()

    // 接続済み
    data class Connected(
        val sessionType: SessionType,
        val subscribeSession: Set<PeerHandle>,
    ) : WifiAwareState()

    // エラー
    data class Error(val throwable: Throwable) : WifiAwareState()
}

