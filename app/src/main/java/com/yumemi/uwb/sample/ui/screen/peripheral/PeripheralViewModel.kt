package com.yumemi.uwb.sample.ui.screen.peripheral

import android.content.Context
import androidx.core.uwb.RangingResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yumemi.uwb.sample.uwb.UwbController
import com.yumemi.uwb.sample.uwb.UwbResponder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PeripheralViewModel(context: Context) : ViewModel() {

    private val uwbResponder = UwbController(context)

    private val _rangingResult = MutableStateFlow<RangingResult.RangingResultPosition?>(null)
    val rangingPosition: StateFlow<RangingResult.RangingResultPosition?> = _rangingResult.asStateFlow()

    init {
        viewModelScope.launch {
            uwbResponder.startBlePeripheral()
        }

        // rangingPosition Flowを監視
        viewModelScope.launch {
            uwbResponder.rangingResult.collect { position ->
                _rangingResult.value = position
            }
        }
    }

    fun startUwb() {
        viewModelScope.launch {
            uwbResponder.startUwbRanging()
        }
    }

    override fun onCleared() {
        super.onCleared()
        uwbResponder.cancelRanging()
    }
}
