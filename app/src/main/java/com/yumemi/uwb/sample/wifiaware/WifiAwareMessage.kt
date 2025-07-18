package com.yumemi.uwb.sample.wifiaware

import android.net.wifi.aware.PeerHandle

data class WifiAwareMessage(
    val content: String,
    val sender: PeerHandle,
    val timestamp: Long = System.currentTimeMillis(),
    val sessionType: SessionType,
)

sealed class WifiAwareError {
    data class PermissionDenied(val permission: String) : WifiAwareError()
    data class AttachmentFailed(val reason: String) : WifiAwareError()
    data class SendMessageFailed(val message: String, val peer: PeerHandle?) : WifiAwareError()
    data class SessionError(val sessionType: SessionType, val error: Throwable) : WifiAwareError()
}
