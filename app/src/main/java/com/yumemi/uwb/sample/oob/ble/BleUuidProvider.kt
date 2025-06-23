package com.yumemi.uwb.sample.oob.ble

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import java.util.UUID

object BleUuidProvider {
    @SuppressLint("HardwareIds")
    fun getServiceUuid(context: Context): UUID {
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID,
        )

        return UUID.nameUUIDFromBytes(
            androidId?.toByteArray(Charsets.UTF_8) ?: ByteArray(0),
        )
    }
}
