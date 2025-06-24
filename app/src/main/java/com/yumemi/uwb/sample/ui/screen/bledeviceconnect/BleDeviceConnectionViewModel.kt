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

    private val deviceNames = mapOf(
        DeviceUuid.GREEN to "緑",
        DeviceUuid.RED to "赤",
        DeviceUuid.YELLOW to "黄",
        DeviceUuid.BROWN to "茶"
    )

    init {
        val initialDevices = DeviceUuid.ALL.associateWith { uuid ->
            BleDevice(id = uuid, name = deviceNames[uuid] ?: "Unknown Device")
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

/**
 * Pixel 9 の UUID、固定で4台定義しておく
 */
object DeviceUuid {
    const val GREEN = "50f6971d-9875-33c7-b231-8e2f99bdb811"
    const val RED = "27cd139d-5b54-38d6-9999-089e17cf9c23"
    const val YELLOW = "4247b308-be67-39a6-bd26-00e4d8f469cf"
    const val BROWN = "692ffa86-df4c-317d-8532-4f3f2deb1dba"
    val ALL = listOf(GREEN, RED, YELLOW, BROWN)
}
