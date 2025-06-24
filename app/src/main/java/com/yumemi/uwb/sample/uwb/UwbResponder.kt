package com.yumemi.uwb.sample.uwb

import android.content.Context
import android.util.Log
import androidx.core.uwb.RangingParameters
import androidx.core.uwb.RangingPosition
import androidx.core.uwb.RangingResult
import androidx.core.uwb.UwbComplexChannel
import androidx.core.uwb.UwbControleeSessionScope
import androidx.core.uwb.UwbDevice
import androidx.core.uwb.UwbManager
import com.yumemi.uwb.sample.oob.ble.BleCentralManager
import com.yumemi.uwb.sample.oob.ble.BleUuidProvider
import com.yumemi.uwb.sample.oob.ble.RangingParametersFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Controlee(Guest) 側
 */
class UwbResponder(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    private var rangingJob: Job? = null
    private lateinit var uwbManager: UwbManager
    private var rangingParameters: RangingParameters? = null
    private var controleeSessionScope: UwbControleeSessionScope? = null

    private val _rangingPosition = MutableSharedFlow<RangingPosition>()
    val rangingPosition: Flow<RangingPosition> = _rangingPosition
    private val _rangingResult = MutableSharedFlow<RangingResult.RangingResultPosition>()
    val rangingResult: Flow<RangingResult.RangingResultPosition> = _rangingResult

    // レスポンダーとしての UWB 測距を開始する
    suspend fun startRanging() {
        Log.d(TAG, "startRanging")
        withContext(Dispatchers.Main.immediate) {
            uwbManager = UwbManager.createInstance(context)
            val controleeSession = uwbManager.controleeSessionScope()
            val uuid = BleUuidProvider.getServiceUuid(context)

            // RangingParameters を作り UWB 接続を開始する
            val rangingParameters = RangingParametersFactory(
                addressByteArray = controleeSession.localAddress.address,
                bleCentralManager = BleCentralManager(context, uuid),
            ).create()

            rangingJob = scope.launch {
                controleeSession.prepareSession(rangingParameters).collect { rangingResult ->
                    when (rangingResult) {
                        is RangingResult.RangingResultPosition -> {
                            Log.d(
                                TAG,
                                "device: ${rangingResult.device.address}, position: ${rangingResult.position.logValue()}",
                            )
                            _rangingPosition.emit(rangingResult.position)
                        }

                        is RangingResult.RangingResultPeerDisconnected -> {
                            Log.d(TAG, "Peer disconnected: $rangingResult")
                        }
                    }
                }
            }
        }
    }

    suspend fun fetchUwbControllerParams() {
        uwbManager = UwbManager.createInstance(context)
        controleeSessionScope = uwbManager.controleeSessionScope()

        controleeSessionScope ?: return
        // ホスト側へ送るデータ
        val addressByteArray = controleeSessionScope!!.localAddress.address
        // uuid を取得
        val uuid = BleUuidProvider.getServiceUuid(context)
        // BLE GATT サーバーへ接続し、UWB ホストと接続に必要なパラメーターを送受信する
        val bleCentral = BleCentralManager(context, uuid)
        bleCentral.connectGattServer()
        val uwbControllerParamsByteArray = bleCentral.readCharacteristic()
        val uwbControllerParams = UwbControllerParams.decode(uwbControllerParamsByteArray)
        bleCentral.writeCharacteristic(addressByteArray)
        bleCentral.destroy()

        // パラメーターを作成
        rangingParameters = RangingParameters(
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

    suspend fun startUwbRanging() {
        controleeSessionScope ?: return
        rangingParameters ?: return
        // Flow で UWB デバイスとの接続状況をもらえる
        controleeSessionScope!!.prepareSession(rangingParameters!!).collect { rangingResult ->
            when (rangingResult) {
                is RangingResult.RangingResultPosition -> {
                    _rangingResult.emit(rangingResult)
                }

                is RangingResult.RangingResultPeerDisconnected -> {
                    Log.d(TAG, "Peer disconnected: ${rangingResult.device.address.address}")
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
