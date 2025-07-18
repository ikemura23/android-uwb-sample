package com.yumemi.uwb.sample.wifiaware

import android.content.Context
import android.net.wifi.aware.AttachCallback
import android.net.wifi.aware.DiscoverySessionCallback
import android.net.wifi.aware.PeerHandle
import android.net.wifi.aware.PublishConfig
import android.net.wifi.aware.PublishDiscoverySession
import android.net.wifi.aware.SubscribeDiscoverySession
import android.net.wifi.aware.WifiAwareManager
import android.net.wifi.aware.WifiAwareSession
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.atomic.AtomicInteger

interface WifiAwareController {
    val state: StateFlow<WifiAwareState>
    val receivedMessages: SharedFlow<WifiAwareMessage>
    suspend fun initialize(sessionType: SessionType): Result<Unit>
    suspend fun sendMessage(message: String, targetPeer: PeerHandle? = null): Result<Unit>
    suspend fun disconnect(): Result<Unit>
}

/**
 * Wi-Fi Awareのコントローラー実装
 * @param context アプリケーションコンテキスト
 * @param coroutineScope コルーチンスコープ
 */
class WifiAwareControllerImpl(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
) : WifiAwareController {
    private val callbackChannel = Channel<WifiAwareState>(Channel.UNLIMITED)

    // Wifi Aware
    private val wifiAwareManager: WifiAwareManager by lazy {
        context.getSystemService(WifiAwareManager::class.java)
    }
    private lateinit var wifiAwareSession: WifiAwareSession
    private lateinit var publishSession: PublishDiscoverySession
    private lateinit var subscribeSession: SubscribeDiscoverySession

    // 複数Peer管理
    private val connectedPeersPub = CopyOnWriteArraySet<PeerHandle>()
    private val connectedPeersSub = CopyOnWriteArraySet<PeerHandle>()

    private val messageIdCounter = AtomicInteger(0)

    private val _state = MutableStateFlow<WifiAwareState>(WifiAwareState.Disconnected)
    override val state: StateFlow<WifiAwareState> = _state

    private val _receivedMessages = MutableSharedFlow<WifiAwareMessage>()
    override val receivedMessages: SharedFlow<WifiAwareMessage> = _receivedMessages

    override suspend fun initialize(sessionType: SessionType): Result<Unit> {
        return runCatching {
            wifiAwareManager.attach(
                object : AttachCallback() {
                    override fun onAttached(session: WifiAwareSession) {
                        wifiAwareSession = session
                    }

                    override fun onAttachFailed() {
                        Log.e(TAG, "WiFi Aware セッションのアタッチに失敗しました")
                        _state.update {
                            WifiAwareState.Error(Throwable("WiFi Aware session attach failed"))
                        }
                    }
                },
                null,
            )
        }
    }

    private fun startPublisher() {
        val config = PublishConfig.Builder().setServiceName(SERVICE_NAME).build()

        try {
            wifiAwareSession.publish(
                config,
                object : DiscoverySessionCallback() {
                    override fun onPublishStarted(session: PublishDiscoverySession) {
                        publishSession = session
                        Log.d(TAG, "Publisher セッション開始")
                    }

                    override fun onServiceDiscovered(peerHandle: PeerHandle?, serviceSpecificInfo: ByteArray?, matchFilter: MutableList<ByteArray>?) {
                        Log.d(TAG, "サービス検出: $peerHandle")
                        connectedPeersPub.add(peerHandle)
                    }

                    override fun onMessageReceived(peerHandle: PeerHandle, message: ByteArray) {
                        val msg = String(message)
                        Log.d(TAG, "Publisher 受信: $msg from $peerHandle")
                    }
                },
                null,
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: Missing permission?", e)
            // TODO: 送信エラー処理
        }
    }

    override suspend fun sendMessage(message: String, targetPeer: PeerHandle?): Result<Unit> {
        return runCatching {
            val messageId = messageIdCounter.incrementAndGet()
            connectedPeersPub.forEach { peer ->
                publishSession.sendMessage(
                    peer,
                    messageId,
                    message.toByteArray(),
                )
            }
        }
    }

    override suspend fun disconnect(): Result<Unit> {
        return runCatching {
            // TODO: 切断処理を実装
        }
    }

    companion object {
        private const val TAG = "WifiAwareController"
        private const val SERVICE_NAME = "wifi_aware_demo"
    }
}
