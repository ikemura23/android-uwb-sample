package com.yumemi.uwb.sample.oob.ble

import android.util.Log
import androidx.core.uwb.RangingParameters
import androidx.core.uwb.UwbComplexChannel
import androidx.core.uwb.UwbDevice
import com.yumemi.uwb.sample.uwb.UwbControllerParams

/**
 * UWB ホスト（親）と接続するためのパラメーターを作成するファクトリー
 */
class RangingParametersFactory(
    private val addressByteArray: ByteArray,
    private val bleCentral: BleCentral,
) {
    suspend fun create(): RangingParameters {
        // BLE GATT サーバーへ接続し、UWB ホストと接続に必要なパラメーターを送受信する
        bleCentral.connectGattServer()
        val uwbControllerParamsByteArray = bleCentral.readCharacteristic()
        val uwbControllerParams: UwbControllerParams = UwbControllerParams.decode(uwbControllerParamsByteArray)
        Log.d(TAG, "UWB Controller Params: $uwbControllerParams")
        bleCentral.writeCharacteristic(addressByteArray)
        bleCentral.destroy()

        return RangingParameters(
            uwbConfigType = RangingParameters.CONFIG_MULTICAST_DS_TWR,
            complexChannel = UwbComplexChannel(uwbControllerParams.channel, uwbControllerParams.preambleIndex),
            peerDevices = listOf(UwbDevice.createForAddress(uwbControllerParams.address)),
            updateRateType = RangingParameters.RANGING_UPDATE_RATE_AUTOMATIC,
            sessionId = uwbControllerParams.sessionId,
            sessionKeyInfo = uwbControllerParams.sessionKeyInfo,
            subSessionId = 0, // SESSION_ID_UNSET ？
            subSessionKeyInfo = null, // ？
        )
    }

    companion object {
        private const val TAG = "RangingParametersFactory"
    }
}
