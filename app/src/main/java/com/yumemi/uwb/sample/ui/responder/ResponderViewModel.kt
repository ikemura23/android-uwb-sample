package com.yumemi.uwb.sample.ui.responder

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.uwb.RangingPosition
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yumemi.uwb.sample.uwb.UwbResponder
import kotlinx.coroutines.launch

class ResponderViewModel(private val uwbResponder: UwbResponder) : ViewModel() {
    private val _uwbPosition = mutableStateOf<RangingPosition?>(null)
    val uwbPosition: State<RangingPosition?> = _uwbPosition

    init {
        startRanging()
    }

    private fun startRanging() {
        viewModelScope.launch {
            try {
                // 先にcollectを準備
                launch {
                    uwbResponder.rangingResult.collect { position ->
                        _uwbPosition.value = position
                    }
                }
                
                // その後でstartRangingを実行
                uwbResponder.startRanging()
            } catch (e: Exception) {
                Log.e("ResponderViewModel", "Failed to start ranging", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        uwbResponder.cancelRanging()
    }
}
