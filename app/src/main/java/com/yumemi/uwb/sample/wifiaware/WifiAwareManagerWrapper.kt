package com.yumemi.uwb.sample.wifiaware

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.aware.AttachCallback
import android.net.wifi.aware.DiscoverySessionCallback
import android.net.wifi.aware.PeerHandle
import android.net.wifi.aware.PublishConfig
import android.net.wifi.aware.PublishDiscoverySession
import android.net.wifi.aware.SubscribeConfig
import android.net.wifi.aware.SubscribeDiscoverySession
import android.net.wifi.aware.WifiAwareManager
import android.net.wifi.aware.WifiAwareSession
import android.util.Log
import androidx.core.content.ContextCompat
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.atomic.AtomicInteger

class WifiAwareManagerWrapper(
    private val context: Context,
    val onMessageReceived: (PeerHandle, String) -> Unit = { _, _ -> },
    val onAwareUnavailable: () -> Unit = { },
) {

    private var wifiAwareManager: WifiAwareManager? = null
    private var wifiAwareSession: WifiAwareSession? = null
    private var publishSession: PublishDiscoverySession? = null
    private var subscribeSession: SubscribeDiscoverySession? = null

    // 複数Peer管理
    private val connectedPeers_pub = CopyOnWriteArraySet<PeerHandle>()
    private val connectedPeers_sub = CopyOnWriteArraySet<PeerHandle>()

    private val messageIdCounter = AtomicInteger(0)

    fun initialize() {

        wifiAwareManager = context.getSystemService(WifiAwareManager::class.java)

        if (!hasRequiredPermission()) {
            Log.w("WiFiAware", "Permission denied: required location or nearby devices permission.")
            onAwareUnavailable()
            return
        }

        wifiAwareManager?.attach(
            object : AttachCallback() {
                override fun onAttached(session: WifiAwareSession) {
                    wifiAwareSession = session
                    startPublisher()
                    startSubscriber()
                }

                override fun onAttachFailed() {
                    Log.e(TAG, "WiFi Aware セッションのアタッチに失敗しました")
                }
            },
            null,
        )
    }

    private fun startPublisher() {

        val config = PublishConfig.Builder().setServiceName(SERVICE_NAME).build()

        try {
            wifiAwareSession?.publish(
                config,
                object : DiscoverySessionCallback() {
                    override fun onPublishStarted(session: PublishDiscoverySession) {
                        publishSession = session
                        Log.d(TAG, "Publisher セッション開始")
                    }

                    override fun onServiceDiscovered(peerHandle: PeerHandle?, serviceSpecificInfo: ByteArray?, matchFilter: MutableList<ByteArray>?) {
                        Log.d(TAG, "サービス検出: $peerHandle")
                        connectedPeers_pub.add(peerHandle)
                    }

                    override fun onMessageReceived(peerHandle: PeerHandle, message: ByteArray) {
                        val msg = String(message)
                        Log.d(TAG, "Publisher 受信: $msg from $peerHandle")
                        onMessageReceived(peerHandle, msg)
                    }
                },
                null,
            )
        } catch (e: SecurityException) {
            Log.e("WiFiAware", "SecurityException: Missing permission?", e)
            onAwareUnavailable()
        }
    }

    private fun startSubscriber() {

        val config = SubscribeConfig.Builder().setServiceName(SERVICE_NAME).build()

        try {
            wifiAwareSession?.subscribe(
                config,
                object : DiscoverySessionCallback() {
                    override fun onSubscribeStarted(session: SubscribeDiscoverySession) {
                        subscribeSession = session
                        Log.d(TAG, "Subscriber セッション開始")
                    }

                    override fun onServiceDiscovered(peerHandle: PeerHandle, serviceInfo: ByteArray?, matchFilter: List<ByteArray>) {
                        Log.d(TAG, "サービス検出: $peerHandle")
                        connectedPeers_sub.add(peerHandle)
                    }

                    override fun onMessageReceived(peerHandle: PeerHandle, message: ByteArray) {
                        val msg = String(message)
                        Log.d(TAG, "Subscriber 受信: $msg from $peerHandle")
                        onMessageReceived(peerHandle, msg)
                    }
                },
                null,
            )
        } catch (e: SecurityException) {
            Log.e("WiFiAware", "SecurityException: Missing permission?", e)
            onAwareUnavailable()
        }
    }

    /**
     * すべての接続済みPeerにメッセージを送信
     */
    fun sendMessageToAll(message: String) {
        val session_pub = publishSession
        if (session_pub == null) {
            Log.e(TAG, "PUB DiscoverySession が未初期化です")
            return
        }

        connectedPeers_pub.forEach { peer ->
            try {
                val id = messageIdCounter.incrementAndGet()
                session_pub.sendMessage(peer, id, message.toByteArray())
                Log.d(TAG, "PUB メッセージ送信: $message to $peer")
            } catch (e: Exception) {
                Log.e(TAG, "PUB メッセージ送信エラー to $peer", e)
            }
        }

        val session_sub = subscribeSession
        if (session_sub == null) {
            Log.e(TAG, "SUB DiscoverySession が未初期化です")
            return
        }

        connectedPeers_sub.forEach { peer ->
            try {
                val id = messageIdCounter.incrementAndGet()
                session_sub.sendMessage(peer, id, message.toByteArray())
                Log.d(TAG, "SUB メッセージ送信: $message to $peer")
            } catch (e: Exception) {
                Log.e(TAG, "SUB メッセージ送信エラー to $peer", e)
            }
        }
    }

    /**
     * 特定Peerに送信
     */
    fun sendMessageTo(peer: PeerHandle, message: String) {
        val session = publishSession ?: subscribeSession
        if (session == null) {
            Log.e(TAG, "DiscoverySession が未初期化です")
            return
        }

        try {
            val id = messageIdCounter.incrementAndGet()
            session.sendMessage(peer, id, message.toByteArray())
            Log.d(TAG, "メッセージ送信: $message to $peer")
        } catch (e: Exception) {
            Log.e(TAG, "メッセージ送信エラー to $peer", e)
        }
    }

    fun getConnectedPeers_pub(): Set<PeerHandle> = connectedPeers_pub.toSet()
    fun getConnectedPeers_sub(): Set<PeerHandle> = connectedPeers_sub.toSet()

    private fun hasRequiredPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val nearby = ContextCompat.checkSelfPermission(context, Manifest.permission.NEARBY_WIFI_DEVICES)
        return fineLocation == PackageManager.PERMISSION_GRANTED || nearby == PackageManager.PERMISSION_GRANTED
    }

    fun close() {
        publishSession?.close()
        subscribeSession?.close()
        wifiAwareSession?.close()
        wifiAwareManager = null
        publishSession = null
        subscribeSession = null
        wifiAwareSession = null
        connectedPeers_pub.clear()
        connectedPeers_sub.clear()
    }

    companion object {
        private const val TAG = "WifiAwareWrapper"
        private const val SERVICE_NAME = "wifi_aware_demo"
    }
}
