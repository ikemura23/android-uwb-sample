package com.yumemi.uwb.sample.ui.screen.bledeviceconnect

import android.content.Context
import android.util.Log
import androidx.core.uwb.RangingParameters
import androidx.core.uwb.RangingResult
import androidx.core.uwb.UwbDevice
import androidx.core.uwb.UwbManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yumemi.uwb.sample.oob.ble.BlePeripheralManager
import com.yumemi.uwb.sample.uwb.UwbControllerParams
import com.yumemi.uwb.sample.uwb.logValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

data class BleDevice(
    val id: String,
    val name: String,
    val isLoading: Boolean = false,
    val isBleConnected: Boolean = false,
    val isUwbLoading: Boolean = false,
    val isUwbConnected: Boolean = false,
    val uwbDevice: UwbDevice? = null,
)

data class BleDeviceConnectionUiState(
    val devices: Map<String, BleDevice> = emptyMap(),
    val isRangingActive: Boolean = false,
)

class BleDeviceConnectionViewModel : ViewModel() {
    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    private val _uiState = MutableStateFlow(BleDeviceConnectionUiState())
    val uiState: StateFlow<BleDeviceConnectionUiState> = _uiState.asStateFlow()
    private var rangingJob: Job? = null

    // セッションIDとセッションキー情報はランダムに生成
    private val sessionId: Int = Random.nextInt()
    private val sessionKeyInfo: ByteArray = Random.nextBytes(8)

    private lateinit var rangingParameters: RangingParameters

    init {
        val initialDevices = DeviceType.ALL.associate { deviceType ->
            deviceType.uuid to BleDevice(id = deviceType.uuid, name = deviceType.displayName)
        }
        _uiState.value = BleDeviceConnectionUiState(devices = initialDevices)
    }

    fun onDeviceClick(
        context: Context,
        deviceId: String,
    ) {
        Log.d(TAG, "onDeviceClick deviceId: $deviceId")
        _uiState.update { currentState ->
            val updatedDevice = currentState.devices[deviceId]?.copy(isLoading = true)
            if (updatedDevice != null) {
                currentState.copy(devices = currentState.devices + (deviceId to updatedDevice))
            } else {
                currentState
            }
        }

        viewModelScope.launch {
            try {
                val uwbManager = UwbManager.createInstance(context)
                val controllerSession = uwbManager.controllerSessionScope()
                Log.d(TAG, "controllerSession.uwbComplexChannel: ${controllerSession.uwbComplexChannel}")

                val uwbControllerParams = UwbControllerParams(
                    address = controllerSession.localAddress.address,
                    channel = controllerSession.uwbComplexChannel.channel,
                    preambleIndex = controllerSession.uwbComplexChannel.preambleIndex,
                    sessionId = sessionId,
                    sessionKeyInfo = sessionKeyInfo,
                )
                // バイト配列に
                val encodeHostParameter = UwbControllerParams.encode(uwbControllerParams)
                // Controlee 側からアドレスが送られてきたら入れる Flow
                val controleeAddressFlow = MutableStateFlow<ByteArray?>(null)
                // BLE の開始
                Log.d(TAG, "BLE の開始")
                val peripheralJob = launch {
                    BlePeripheralManager.startPeripheralAndAdvertising(
                        context = context,
                        serviceUuid = UUID.fromString(deviceId),
                        onCharacteristicReadRequest = {
                            // controlee へ送る
                            encodeHostParameter
                        },
                        onCharacteristicWriteRequest = {
                            // controlee から受け取る
                            controleeAddressFlow.value = it
                        },
                    )
                }
                Log.d(TAG, "アドレスが送られてきたらペリフェラル終了")
                // アドレスが送られてきたらペリフェラル終了
                val controleeAddress = controleeAddressFlow.filterNotNull().first()
                peripheralJob.cancel()
                Log.d(TAG, "RangingParameters を作り UWB 接続を開始する")
                // RangingParameters を作り UWB 接続を開始する
                rangingParameters = RangingParameters(
                    uwbConfigType = RangingParameters.CONFIG_MULTICAST_DS_TWR,
                    complexChannel = controllerSession.uwbComplexChannel,
                    peerDevices = listOf(UwbDevice.createForAddress(controleeAddress)),
                    updateRateType = RangingParameters.RANGING_UPDATE_RATE_AUTOMATIC,
                    sessionId = sessionId,
                    sessionKeyInfo = sessionKeyInfo,
                    subSessionId = 0, // SUB_SESSION_UNSET
                    subSessionKeyInfo = null, // 暗号化の何か
                )
                Log.d(TAG, "rangingParameters: $rangingParameters")
                _uiState.update { currentState ->
                    val deviceToUpdate = currentState.devices[deviceId]
                    if (deviceToUpdate != null) {
                        val updatedDevice = deviceToUpdate.copy(
                            isLoading = false,
                            isBleConnected = true,
                            // uwbDevice = uwbDevice,
                        )
                        currentState.copy(devices = currentState.devices + (deviceId to updatedDevice))
                    } else {
                        currentState
                    }
                }

                /////////
                // val bleCentralManager = BleCentralManager(context, UUID.fromString(deviceId))
                // // BLE GATT サーバーへ接続し、UWB ゲスト と接続に必要なパラメーターを送受信する
                // bleCentralManager.connectGattServer()
                // val uwbControllerParamsByteArray = bleCentralManager.readCharacteristic()
                // val uwbControllerParams: UwbControllerParams = UwbControllerParams.decode(uwbControllerParamsByteArray)
                // Log.d(TAG, "UWB Controller Params: $uwbControllerParams")
                // val addressByteArray = UwbManager.createInstance(context).controleeSessionScope().localAddress.address
                // bleCentralManager.writeCharacteristic(addressByteArray)
                // bleCentralManager.destroy()
                //
                // val uwbDevice = UwbDevice.createForAddress(uwbControllerParams.address)
                //
                // _uiState.update { currentState ->
                //     val deviceToUpdate = currentState.devices[deviceId]
                //     if (deviceToUpdate != null) {
                //         val updatedDevice = deviceToUpdate.copy(
                //             isLoading = false,
                //             isBleConnected = true,
                //             uwbDevice = uwbDevice,
                //         )
                //         currentState.copy(devices = currentState.devices + (deviceId to updatedDevice))
                //     } else {
                //         currentState
                //     }
                // }
            } catch (e: Exception) {
                Log.e("BleDeviceConnectionViewModel", "Failed to get ranging parameters for $deviceId", e)
            }
        }
    }

    fun startRanging(context: Context) {
        val bleConnectedDevices: List<BleDevice> = _uiState.value.devices.values.filter { it.isBleConnected && it.uwbDevice != null }

        if (bleConnectedDevices.isEmpty()) {
            Log.w("BleDeviceConnectionViewModel", "No connected devices with UWB devices")
            return
        }

        val uwbDevices: List<UwbDevice> = bleConnectedDevices.mapNotNull { it.uwbDevice }
        Log.d(TAG, "uwbDevices: ${uwbDevices.size}")

        _uiState.update { it.copy(isRangingActive = true) }

        viewModelScope.launch {
            try {
                // rangingJob = scope.launch {
                Log.d(TAG, "scope.launch")
                val uwbManager = UwbManager.createInstance(context)
                val controllerSession = uwbManager.controllerSessionScope()

                Log.d(TAG, "RangingParametersの作成")
                // RangingParametersの作成
                val rangingParameters = RangingParameters(
                    uwbConfigType = RangingParameters.CONFIG_MULTICAST_DS_TWR,
                    complexChannel = controllerSession.uwbComplexChannel,
                    peerDevices = uwbDevices,
                    updateRateType = RangingParameters.RANGING_UPDATE_RATE_AUTOMATIC,
                    sessionId = sessionId,
                    sessionKeyInfo = sessionKeyInfo,
                    subSessionId = 0,
                    subSessionKeyInfo = null,
                )

                Log.d(TAG, "rangingParameters: $rangingParameters")
                controllerSession.prepareSession(rangingParameters).collect { rangingResult ->
                    when (rangingResult) {
                        is RangingResult.RangingResultPosition -> {
                            Log.d(TAG, "UWB通信成功!! rangingResult.position: ${rangingResult.position.logValue()}")
                            // _rangingPosition.emit(rangingResult.position)
                        }

                        is RangingResult.RangingResultPeerDisconnected ->
                            Log.d(TAG, "Peer disconnected")
                    }
                }
                // }
                // 接続済みのデバイスに対して測距を開始
            } catch (e: Exception) {
                Log.e("BleDeviceConnectionViewModel", "Failed to start ranging", e)
                _uiState.update { it.copy(isRangingActive = false) }
            }
        }
    }

    fun cancelRanging() {
        Log.d("BleDeviceConnectionViewModel", "Canceling ranging for all devices")

        rangingJob?.cancel()

        // UI状態を更新
        _uiState.update { currentState ->
            val updatedDevices = currentState.devices.mapValues { (_, device) ->
                device.copy(isUwbConnected = false)
            }
            currentState.copy(
                devices = updatedDevices,
                isRangingActive = false,
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        cancelRanging()
    }

    companion object {
        private const val TAG = "BleDeviceConnectionViewModel"
    }
}
