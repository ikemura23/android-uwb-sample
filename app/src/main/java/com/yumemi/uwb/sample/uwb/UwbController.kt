package com.yumemi.uwb.sample.uwb

import android.content.Context
import android.util.Log
import androidx.core.uwb.RangingParameters
import androidx.core.uwb.RangingPosition
import androidx.core.uwb.RangingResult
import androidx.core.uwb.UwbDevice
import androidx.core.uwb.UwbManager
import com.yumemi.uwb.sample.oob.ble.BlePeripheralManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class UwbController(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    private var rangingJob: Job? = null
    private lateinit var uwbManager: UwbManager
    private val _rangingPosition = MutableSharedFlow<RangingPosition>()
    private var rangingParameters: RangingParameters? = null
    val rangingPosition: Flow<RangingPosition> = _rangingPosition

    suspend fun startBlePeripheral() {
        Log.d(TAG, "startBlePeripheral")
        uwbManager = UwbManager.createInstance(context)
        val controllerSession = uwbManager.controllerSessionScope()

        val sessionId = Random.nextInt()
        val sessionKeyInfo = Random.nextBytes(8)
        val uwbControllerParams = UwbControllerParams(
            address = controllerSession.localAddress.address,
            channel = controllerSession.uwbComplexChannel.channel,
            preambleIndex = controllerSession.uwbComplexChannel.preambleIndex,
            sessionId = sessionId,
            sessionKeyInfo = sessionKeyInfo,
        )
        val encodeHostParameter = UwbControllerParams.encode(uwbControllerParams)

        val controleeAddressFlow = MutableStateFlow<ByteArray?>(null)

        Log.d(TAG, "ペリフェラルを開始")
        // BLE ペリフェラルを開始
        val peripheralJob = scope.launch {
            BlePeripheralManager.startPeripheralAndAdvertising(
                context = context,
                onCharacteristicReadRequest = { encodeHostParameter },
                onCharacteristicWriteRequest = { controleeAddressFlow.value = it },
            )
        }
        Log.d(TAG, "アドレスが送られてくるまで待機")
        // アドレスが送られてくるまで待機
        val controleeAddress = controleeAddressFlow.filterNotNull().first()
        val uwbDevice: UwbDevice = UwbDevice.createForAddress(controleeAddress)
        Log.d(TAG, "uwbDevice.address: ${uwbDevice.address}")
        peripheralJob.cancel()

        Log.d(TAG, "RangingParameters 作成")
        rangingParameters = RangingParameters(
            uwbConfigType = RangingParameters.CONFIG_MULTICAST_DS_TWR,
            complexChannel = controllerSession.uwbComplexChannel,
            peerDevices = listOf(uwbDevice),
            updateRateType = RangingParameters.RANGING_UPDATE_RATE_AUTOMATIC,
            sessionId = sessionId,
            sessionKeyInfo = sessionKeyInfo,
            subSessionId = 0,
            subSessionKeyInfo = null,
        )
    }

    suspend fun startUwbRanging() {
        Log.d(TAG, "startUwbRanging")
        rangingParameters ?: return

        val controllerSession = uwbManager.controllerSessionScope()
        rangingJob = scope.launch {

            controllerSession.prepareSession(rangingParameters!!).collect { rangingResult ->
                when (rangingResult) {
                    is RangingResult.RangingResultPosition -> {
                        Log.d("UwbController", "device: ${rangingResult.device.address}, position: ${rangingResult.position.logValue()}")
                        _rangingPosition.emit(rangingResult.position)
                    }

                    is RangingResult.RangingResultPeerDisconnected ->
                        Log.d(TAG, "Peer disconnected: $rangingResult")
                }
            }
        }
    }

    // 制御対象の UWB 測距を開始する
    suspend fun startRanging() {
        Log.d(TAG, "startRanging")
        withContext(Dispatchers.Main.immediate) {
            uwbManager = UwbManager.createInstance(context)
            val controllerSession = uwbManager.controllerSessionScope()

            val sessionId = Random.nextInt()
            val sessionKeyInfo = Random.nextBytes(8)
            val uwbControllerParams = UwbControllerParams(
                address = controllerSession.localAddress.address,
                channel = controllerSession.uwbComplexChannel.channel,
                preambleIndex = controllerSession.uwbComplexChannel.preambleIndex,
                sessionId = sessionId,
                sessionKeyInfo = sessionKeyInfo,
            )
            var rangingParameters: RangingParameters? = null
            val encodeHostParameter = UwbControllerParams.encode(uwbControllerParams)

            val controleeAddressFlow = MutableStateFlow<ByteArray?>(null)

            Log.d(TAG, "ペリフェラルを開始")
            // BLE ペリフェラルを開始
            val peripheralJob = scope.launch {
                BlePeripheralManager.startPeripheralAndAdvertising(
                    context = context,
                    onCharacteristicReadRequest = { encodeHostParameter },
                    onCharacteristicWriteRequest = { controleeAddressFlow.value = it },
                )
            }
            Log.d(TAG, "アドレスが送られてくるまで待機")
            // アドレスが送られてくるまで待機
            val controleeAddress = controleeAddressFlow.filterNotNull().first()
            val uwbDevice: UwbDevice = UwbDevice.createForAddress(controleeAddress)
            Log.d(TAG, "uwbDevice.address: ${uwbDevice.address}")
            peripheralJob.cancel()

            Log.d(TAG, "RangingParameters 作成")
            rangingParameters = RangingParameters(
                uwbConfigType = RangingParameters.CONFIG_MULTICAST_DS_TWR,
                complexChannel = controllerSession.uwbComplexChannel,
                peerDevices = listOf(uwbDevice),
                updateRateType = RangingParameters.RANGING_UPDATE_RATE_AUTOMATIC,
                sessionId = sessionId,
                sessionKeyInfo = sessionKeyInfo,
                subSessionId = 0,
                subSessionKeyInfo = null,
            )
            Log.d(TAG, "prepareSessionで測距を開始")
            // 測距を開始
            rangingJob = scope.launch {
                controllerSession.prepareSession(rangingParameters).collect { rangingResult ->
                    when (rangingResult) {
                        is RangingResult.RangingResultPosition -> {
                            Log.d("UwbController", "device: ${rangingResult.device.address}, position: ${rangingResult.position.logValue()}")
                            _rangingPosition.emit(rangingResult.position)
                        }

                        is RangingResult.RangingResultPeerDisconnected ->
                            Log.d(TAG, "Peer disconnected: $rangingResult")
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
        private const val TAG = "UwbController"
    }
}
