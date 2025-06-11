package com.yumemi.uwb.sample.uwb

import androidx.core.uwb.RangingPosition

fun RangingPosition.logValue(): String {
    return "RangingPosition(" +
        "azimuth=${azimuth?.value}, " +
        "distance=${distance?.value}, " +
        "timestampNanos=$elapsedRealtimeNanos, " +
        "elevation=${elevation?.value}, "
}
