package com.yumemi.uwb.sample.ui.controller

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yumemi.uwb.sample.wifiaware.WifiAwareManagerWrapper
import kotlinx.coroutines.launch

class ControllerViewModel : ViewModel() {
    private lateinit var handler: Handler
    private lateinit var sendMessageRunnable: Runnable
    private var isAutoSendingEnabled = false
    private lateinit var wifiAwareManager: WifiAwareManagerWrapper

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
            handler = Handler(Looper.getMainLooper())
            sendMessageRunnable = object : Runnable {
                override fun run() {
                    if (isAutoSendingEnabled) {
                        wifiAwareManager.sendMessageToAll("AUTO Message - ${System.currentTimeMillis()}")
                        handler.postDelayed(this, 2000)
                    }
                }
            }
        }
    }

    fun toggleAutoSending() {
        if (isAutoSendingEnabled) {
            isAutoSendingEnabled = false
            handler.removeCallbacks(sendMessageRunnable)
            Log.d(TAG, "Auto sending stopped")
        } else {
            isAutoSendingEnabled = true
            handler.post(sendMessageRunnable)
            Log.d(TAG, "Auto sending started")
        }
    }

    companion object {
        private const val TAG = "ControllerViewModel"
    }
}
