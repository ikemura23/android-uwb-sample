package com.yumemi.uwb.sample.ui.screen.bledeviceconnect

import android.content.Context
import android.util.Log
import androidx.core.uwb.RangingParameters
import androidx.core.uwb.RangingResult
import androidx.core.uwb.UwbManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yumemi.uwb.sample.oob.ble.BleCentralManager
import com.yumemi.uwb.sample.oob.ble.RangingParametersFactory
import com.yumemi.uwb.sample.uwb.logValue
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
    val isUwbConnected: Boolean = false,
    val rangingParameters: RangingParameters? = null,
)

data class BleDeviceConnectionUiState(
    val devices: Map<String, BleDevice> = emptyMap(),
    val isRangingActive: Boolean = false,
)

class BleDeviceConnectionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BleDeviceConnectionUiState())
    val uiState: StateFlow<BleDeviceConnectionUiState> = _uiState.asStateFlow()

    private var rangingJobs = mutableMapOf<String, Job>()

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
            val rangingParameters = try {
                RangingParametersFactory(
                    addressByteArray = UwbManager.createInstance(context).controleeSessionScope().localAddress.address,
                    bleCentralManager = BleCentralManager(context, UUID.fromString(deviceId)),
                ).create()
            } catch (e: Exception) {
                Log.e("BleDeviceConnectionViewModel", "Failed to get ranging parameters for $deviceId", e)
                null
            }

            _uiState.update { currentState ->
                val deviceToUpdate = currentState.devices[deviceId]
                if (deviceToUpdate != null) {
                    val updatedDevice = deviceToUpdate.copy(
                        isLoading = false,
                        isBleConnected = rangingParameters != null,
                        rangingParameters = rangingParameters,
                    )
                    currentState.copy(devices = currentState.devices + (deviceId to updatedDevice))
                } else {
                    currentState
                }
            }
        }
    }

    fun startRanging(context: Context) {
        val connectedDevices = _uiState.value.devices.values.filter { it.isBleConnected && it.rangingParameters != null }

        if (connectedDevices.isEmpty()) {
            Log.w("BleDeviceConnectionViewModel", "No connected devices with ranging parameters")
            return
        }

        _uiState.update { it.copy(isRangingActive = true) }

        viewModelScope.launch {
            try {
                val uwbManager = UwbManager.createInstance(context)
                val controllerSession = uwbManager.controllerSessionScope()

                // 接続済みのデバイスに対して測距を開始
                connectedDevices.forEach { device ->
                    device.rangingParameters?.let { rangingParameters ->
                        Log.d("BleDeviceConnectionViewModel", "Starting ranging for device: ${device.name}")

                        val rangingJob = viewModelScope.launch {
                            controllerSession.prepareSession(rangingParameters).collect { rangingResult ->
                                when (rangingResult) {
                                    is RangingResult.RangingResultPosition -> {
                                        Log.d(
                                            "BleDeviceConnectionViewModel",
                                            "Device: ${device.name}, Position: ${rangingResult.position.logValue()}",
                                        )

                                        // デバイスの状態を更新（UWB接続済みに）
                                        _uiState.update { currentState ->
                                            val deviceToUpdate = currentState.devices[device.id]
                                            if (deviceToUpdate != null) {
                                                val updatedDevice = deviceToUpdate.copy(
                                                    isUwbConnected = true,
                                                )
                                                currentState.copy(
                                                    devices = currentState.devices + (device.id to updatedDevice),
                                                )
                                            } else {
                                                currentState
                                            }
                                        }
                                    }

                                    is RangingResult.RangingResultPeerDisconnected -> {
                                        Log.d(
                                            "BleDeviceConnectionViewModel",
                                            "Peer disconnected for device: ${device.name}",
                                        )

                                        // デバイスの状態を更新（UWB接続解除）
                                        _uiState.update { currentState ->
                                            val deviceToUpdate = currentState.devices[device.id]
                                            if (deviceToUpdate != null) {
                                                val updatedDevice = deviceToUpdate.copy(
                                                    isUwbConnected = false,
                                                )
                                                currentState.copy(
                                                    devices = currentState.devices + (device.id to updatedDevice),
                                                )
                                            } else {
                                                currentState
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Jobを保存
                        rangingJobs[device.id] = rangingJob
                    }
                }
            } catch (e: Exception) {
                Log.e("BleDeviceConnectionViewModel", "Failed to start ranging", e)
                _uiState.update { it.copy(isRangingActive = false) }
            }
        }
    }

    fun cancelRanging() {
        Log.d("BleDeviceConnectionViewModel", "Canceling ranging for all devices")

        // すべてのrangingJobをキャンセル
        rangingJobs.values.forEach { job ->
            job.cancel()
        }
        rangingJobs.clear()

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
}
