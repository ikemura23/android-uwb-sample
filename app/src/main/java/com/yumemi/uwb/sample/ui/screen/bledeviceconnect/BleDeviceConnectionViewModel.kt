package com.yumemi.uwb.sample.ui.screen.bledeviceconnect

import android.content.Context
import android.util.Log
import androidx.core.uwb.UwbManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yumemi.uwb.sample.oob.ble.BleCentralManager
import com.yumemi.uwb.sample.oob.ble.RangingParametersFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class BleDevice(
    val id: String,
    val name: String,
    val uuid: String,
    val isConnected: Boolean = false,
)

data class BleDeviceConnectionUiState(
    val devices: List<BleDevice> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

class BleDeviceConnectionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BleDeviceConnectionUiState())
    val uiState: StateFlow<BleDeviceConnectionUiState> = _uiState.asStateFlow()

    init {
        loadInitialDevices()
    }

    private fun loadInitialDevices() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
            )

            // 初期デバイスリストを作成
            val initialDevices = listOf(
                BleDevice(
                    id = "device1",
                    name = "デバイス1",
                    uuid = "1234-5678-9012-3456",
                ),
                BleDevice(
                    id = "device2",
                    name = "デバイス2",
                    uuid = "2345-6789-0123-4567",
                ),
                BleDevice(
                    id = "device3",
                    name = "デバイス3",
                    uuid = "3456-7890-1234-5678",
                ),
                BleDevice(
                    id = "device4",
                    name = "デバイス4",
                    uuid = "4567-8901-2345-6789",
                ),
            )

            _uiState.value = _uiState.value.copy(
                devices = initialDevices,
                isLoading = false,
            )
        }
    }
    //
    // fun onDeviceClick(deviceId: String) {
    //     viewModelScope.launch {
    //         val currentDevices = _uiState.value.devices.toMutableList()
    //         val deviceIndex = currentDevices.indexOfFirst { it.id == deviceId }
    //
    //         if (deviceIndex != -1) {
    //             val device = currentDevices[deviceIndex]
    //             val updatedDevice = device.copy(isConnected = !device.isConnected)
    //             currentDevices[deviceIndex] = updatedDevice
    //
    //             _uiState.value = _uiState.value.copy(
    //                 devices = currentDevices,
    //             )
    //         }
    //     }
    // }

    fun connectToDevice(deviceId: String) {
        viewModelScope.launch {
            // ここで実際のBLE接続処理を実装
            // 現在は状態の切り替えのみ
        }
    }

    fun disconnectFromDevice(deviceId: String) {
        viewModelScope.launch {
            // ここで実際のBLE切断処理を実装
            // 現在は状態の切り替えのみ
        }
    }

    /**
     * デバイス 1 クリックイベントハンドラー
     */
    fun onDevice1Click(context: Context) {
        viewModelScope.launch {
            val rangingParameters = RangingParametersFactory(
                addressByteArray = UwbManager.createInstance(context).controleeSessionScope().localAddress.address,
                bleCentralManager = BleCentralManager(context),
                uuid = UUID.fromString(DeviceUuid.GREEN),
            ).create()
            // rangingParameters が取得できればOK
            Log.d("BleDeviceConnectionViewModel", "Ranging Parameters for Device 1 GREEN: $rangingParameters")
        }
    }

    /**
     * デバイス 2 クリックイベントハンドラー
     */
    fun onDevice2Click(context: Context) {
        viewModelScope.launch {
            val rangingParameters = RangingParametersFactory(
                addressByteArray = UwbManager.createInstance(context).controleeSessionScope().localAddress.address,
                bleCentralManager = BleCentralManager(context),
                uuid = UUID.fromString(DeviceUuid.RED),
            ).create()
            // rangingParameters が取得できればOK
            Log.d("BleDeviceConnectionViewModel", "Ranging Parameters for Device 2 RED: $rangingParameters")
        }
    }

    /**
     * デバイス 3 クリックイベントハンドラー
     */
    fun onDevice3Click(context: Context) {
        viewModelScope.launch {
            val rangingParameters = RangingParametersFactory(
                addressByteArray = UwbManager.createInstance(context).controleeSessionScope().localAddress.address,
                bleCentralManager = BleCentralManager(context),
                uuid = UUID.fromString(DeviceUuid.YELLOW),
            ).create()
            // rangingParameters が取得できればOK
            Log.d("BleDeviceConnectionViewModel", "Ranging Parameters for Device 3 YELLOW: $rangingParameters")
        }
    }

    /**
     * デバイス 4 クリックイベントハンドラー
     */
    fun onDevice4Click(context: Context) {
        viewModelScope.launch {
            val rangingParameters = RangingParametersFactory(
                addressByteArray = UwbManager.createInstance(context).controleeSessionScope().localAddress.address,
                bleCentralManager = BleCentralManager(context),
                uuid = UUID.fromString(DeviceUuid.BROWN),
            ).create()
            // rangingParameters が取得できればOK
            Log.d("BleDeviceConnectionViewModel", "Ranging Parameters for Device 4 BROWN: $rangingParameters")
        }
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
}
