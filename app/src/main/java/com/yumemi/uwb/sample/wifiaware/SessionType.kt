package com.yumemi.uwb.sample.wifiaware

/**
 * Wifi Awareのセッションの種類を表す列挙型
 *
 * - [PUBLISHER]: 発信側セッション
 * - [SUBSCRIBER]: 受信側セッション
 * - [BOTH]: 両方のセッション
 */
enum class SessionType {
    // 発信
    PUBLISHER,

    // 受信
    SUBSCRIBER,

    // 両方
    BOTH
}
