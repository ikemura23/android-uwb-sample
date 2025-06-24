package com.yumemi.uwb.sample.ui.screen.bledeviceconnect

import android.content.Context
import android.util.Log
import androidx.core.uwb.UwbDevice
import androidx.core.uwb.UwbManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yumemi.uwb.sample.oob.ble.BleCentralManager
import com.yumemi.uwb.sample.uwb.UwbControllerParams
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

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

    private val _uiState = MutableStateFlow(BleDeviceConnectionUiState())
    val uiState: StateFlow<BleDeviceConnectionUiState> = _uiState.asStateFlow()
    private var rangingJob: Job? = null

    init {
        val initialDevices = DeviceType.ALL.associate { deviceType ->
            deviceType.uuid to BleDevice(id = deviceType.uuid, name = deviceType.displayName)
        }
        _uiState.value = BleDeviceConnectionUiState(devices = initialDevices)
    }

    fun onDeviceClick(context: Context, deviceId: String) {
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
                val bleCentralManager = BleCentralManager(context, UUID.fromString(deviceId))
                // BLE GATT サーバーへ接続し、UWB ゲスト と接続に必要なパラメーターを送受信する
                bleCentralManager.connectGattServer()
                val uwbControllerParamsByteArray = bleCentralManager.readCharacteristic()
                val uwbControllerParams: UwbControllerParams = UwbControllerParams.decode(uwbControllerParamsByteArray)
                Log.d(TAG, "UWB Controller Params: $uwbControllerParams")
                val addressByteArray = UwbManager.createInstance(context).controleeSessionScope().localAddress.address
                bleCentralManager.writeCharacteristic(addressByteArray)
                bleCentralManager.destroy()

                val uwbDevice = UwbDevice.createForAddress(uwbControllerParams.address)

                _uiState.update { currentState ->
                    val deviceToUpdate = currentState.devices[deviceId]
                    if (deviceToUpdate != null) {
                        val updatedDevice = deviceToUpdate.copy(
                            isLoading = false,
                            isBleConnected = true,
                            uwbDevice = uwbDevice,
                        )
                        currentState.copy(devices = currentState.devices + (deviceId to updatedDevice))
                    } else {
                        currentState
                    }
                }
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
        Log.d(TAG, "uwbDevices: $uwbDevices")

        _uiState.update { it.copy(isRangingActive = true) }


        viewModelScope.launch {
            try {
                val uwbManager = UwbManager.createInstance(context)
                val controllerSession = uwbManager.controllerSessionScope()
                // val rangingParameters = RangingParameters(
                //     uwbConfigType = RangingParameters.CONFIG_MULTICAST_DS_TWR,
                //     complexChannel = UwbComplexChannel(uwbControllerParams.channel, uwbControllerParams.preambleIndex),
                //     peerDevices = uwbDevices,
                //     updateRateType = RangingParameters.RANGING_UPDATE_RATE_AUTOMATIC,
                //     sessionId = uwbControllerParams.sessionId,
                //     sessionKeyInfo = uwbControllerParams.sessionKeyInfo,
                //     subSessionId = 0, // SESSION_ID_UNSET ？
                //     subSessionKeyInfo = null, // ？
                // )

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
