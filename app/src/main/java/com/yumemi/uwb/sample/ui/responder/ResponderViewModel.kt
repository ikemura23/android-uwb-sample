package com.yumemi.uwb.sample.ui.responder

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.uwb.RangingPosition
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yumemi.uwb.sample.uwb.UwbResponder
import com.yumemi.uwb.sample.wifiaware.WifiAwareManagerWrapper
import kotlinx.coroutines.launch

class ResponderViewModel(private val uwbResponder: UwbResponder) : ViewModel() {
    private val _uwbPosition = mutableStateOf<RangingPosition?>(null)
    val uwbPosition: State<RangingPosition?> = _uwbPosition

    private lateinit var wifiAwareManager: WifiAwareManagerWrapper

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

    fun initializeWifiAware(context: Context) {
        wifiAwareManager = WifiAwareManagerWrapper(
            context = context,
            onMessageReceived = { peerHandle, message ->
                Toast.makeText(context, "Message from peer ${peerHandle}: $message", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Message received from peer ${peerHandle}: $message")
            },
            onAwareUnavailable = {
                Log.d(TAG, "onAwareUnavailable")
            },
        )

        viewModelScope.launch {
            wifiAwareManager.initialize()
        }
    }

    // fun startSubscriber() {
    //     if (::wifiAwareManager.isInitialized) {
    //         Log.d(TAG, "WiFi Aware Subscriber already initialized")
    //     } else {
    //         Log.w(TAG, "WiFi Aware Manager not initialized. Call initializeWifiAware() first.")
    //     }
    // }

    override fun onCleared() {
        super.onCleared()
        uwbResponder.cancelRanging()
    }

    companion object {
        private const val TAG = "ResponderViewModel"
    }
}
