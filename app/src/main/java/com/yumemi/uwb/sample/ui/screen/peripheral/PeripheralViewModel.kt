package com.yumemi.uwb.sample.ui.screen.peripheral

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yumemi.uwb.sample.uwb.UwbController
import kotlinx.coroutines.launch

class PeripheralViewModel(context: Context) : ViewModel() {
    private val uwbController = UwbController(context)

    init {
        viewModelScope.launch {
            uwbController.startRanging()
        }
    }
}
