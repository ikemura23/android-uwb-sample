package com.yumemi.uwb.sample.ui.screen.peripheral

import android.content.Context
import androidx.core.uwb.RangingPosition
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yumemi.uwb.sample.uwb.UwbController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PeripheralViewModel(context: Context) : ViewModel() {

    private val uwbController = UwbController(context)
    
    private val _rangingPosition = MutableStateFlow<RangingPosition?>(null)
    val rangingPosition: StateFlow<RangingPosition?> = _rangingPosition.asStateFlow()

    init {
        viewModelScope.launch {
            uwbController.startBlePeripheral()
        }
        
        // rangingPosition Flowを監視
        viewModelScope.launch {
            uwbController.rangingPosition.collect { position ->
                _rangingPosition.value = position
            }
        }
    }

    fun startUwb() {
        viewModelScope.launch {
            uwbController.startUwbRanging()
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        uwbController.cancelRanging()
    }
}
