package com.yumemi.uwb.sample.uwb

import android.content.Context
import android.util.Log
import androidx.core.uwb.RangingParameters
import androidx.core.uwb.RangingPosition
import androidx.core.uwb.RangingResult
import androidx.core.uwb.UwbComplexChannel
import androidx.core.uwb.UwbDevice
import androidx.core.uwb.UwbManager
import com.yumemi.uwb.sample.oob.ble.BleCentral
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UwbResponder(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    private var rangingJob: Job? = null
    private lateinit var uwbManager: UwbManager
    private val _rangingResult = MutableSharedFlow<RangingPosition>()
    val rangingResult: Flow<RangingPosition> = _rangingResult

    // レスポンダーとしての UWB 測距を開始する
    suspend fun startRanging() {
        Log.d(TAG, "startRanging")
        withContext(Dispatchers.Main.immediate) {
            uwbManager = UwbManager.createInstance(context)
            val controleeSession = uwbManager.controleeSessionScope()

            // controller に送る
            val addressByteArray = controleeSession.localAddress.address

            // BLE GATT サーバーへ接続し、UWB ホストと接続に必要なパラメーターを送受信する
            val bleCentral = BleCentral(context)
            bleCentral.connectGattServer()
            val uwbControllerParamsByteArray = bleCentral.readCharacteristic()
            val uwbControllerParams: UwbControllerParams = UwbControllerParams.decode(uwbControllerParamsByteArray)
            Log.d(TAG, "UWB Controller Params: $uwbControllerParams")
            bleCentral.writeCharacteristic(addressByteArray)
            bleCentral.destroy()

            // RangingParameters を作り UWB 接続を開始する
            val rangingParameters = RangingParameters(
                uwbConfigType = RangingParameters.CONFIG_MULTICAST_DS_TWR,
                complexChannel = UwbComplexChannel(uwbControllerParams.channel, uwbControllerParams.preambleIndex),
                peerDevices = listOf(UwbDevice.createForAddress(uwbControllerParams.address)),
                updateRateType = RangingParameters.RANGING_UPDATE_RATE_AUTOMATIC,
                sessionId = uwbControllerParams.sessionId,
                sessionKeyInfo = uwbControllerParams.sessionKeyInfo,
                subSessionId = 0, // SESSION_ID_UNSET ？
                subSessionKeyInfo = null, // ？
            )

            rangingJob = scope.launch {
                controleeSession.prepareSession(rangingParameters).collect { rangingResult ->
                    when (rangingResult) {
                        is RangingResult.RangingResultPosition -> {
                            Log.d(TAG, "device: ${rangingResult.device.address}, position: ${rangingResult.position.logValue()}")
                            _rangingResult.emit(rangingResult.position)
                        }

                        is RangingResult.RangingResultPeerDisconnected -> {
                            Log.d(TAG, "Peer disconnected: $rangingResult")
                        }
                    }
                }
            }
        }
    }

    fun cancelRanging() {
        rangingJob?.cancel()
        rangingJob = null
        scope.cancel()
    }

    companion object {
        private const val TAG = "UwbResponder"
    }
}
