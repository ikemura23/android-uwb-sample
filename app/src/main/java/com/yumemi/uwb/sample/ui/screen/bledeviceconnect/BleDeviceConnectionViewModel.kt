package com.yumemi.uwb.sample.ui.screen.bledeviceconnect

import android.content.Context
import android.util.Log
import androidx.core.uwb.RangingParameters
import androidx.core.uwb.UwbManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yumemi.uwb.sample.oob.ble.BleCentralManager
import com.yumemi.uwb.sample.oob.ble.RangingParametersFactory
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
)

class BleDeviceConnectionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BleDeviceConnectionUiState())
    val uiState: StateFlow<BleDeviceConnectionUiState> = _uiState.asStateFlow()

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
                        rangingParameters = rangingParameters
                    )
                    currentState.copy(devices = currentState.devices + (deviceId to updatedDevice))
                } else {
                    currentState
                }
            }
        }
    }

    fun startRanging() {

    }
}
