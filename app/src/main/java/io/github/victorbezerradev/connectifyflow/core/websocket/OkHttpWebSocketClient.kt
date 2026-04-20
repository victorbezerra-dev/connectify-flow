package io.github.victorbezerradev.connectifyflow.core.websocket

import io.github.victorbezerradev.connectifyflow.app.di.WebSocketOkHttpClient
import io.github.victorbezerradev.connectifyflow.app.di.WebSocketUrl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OkHttpWebSocketClient
    @Inject
    constructor(
        @param:WebSocketOkHttpClient private val okHttpClient: OkHttpClient,
        @param:WebSocketUrl private val webSocketUrl: String,
    ) : WebSocketClient {
        private val _connectionState =
            MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
        override val connectionState: StateFlow<ConnectionState> = _connectionState

        private val _messages =
            MutableSharedFlow<String>(
                replay = 0,
                extraBufferCapacity = 64,
            )
        override val messages: Flow<String> = _messages.asSharedFlow()

        private var webSocket: WebSocket? = null

        override fun connect() {
            val currentState = _connectionState.value

            if (currentState is ConnectionState.Connected && webSocket != null) {
                return
            }

            webSocket?.cancel()
            webSocket = null

            _connectionState.value = ConnectionState.Connecting

            val request =
                Request.Builder()
                    .url(webSocketUrl)
                    .build()

            webSocket = okHttpClient.newWebSocket(request, createListener())
        }

        override fun send(message: String): Boolean {
            val currentSocket = webSocket
            val isConnected = _connectionState.value is ConnectionState.Connected

            return if (currentSocket != null && isConnected) {
                currentSocket.send(message)
            } else {
                false
            }
        }

        override fun disconnect() {
            val currentSocket = webSocket
            webSocket = null

            currentSocket?.close(NORMAL_CLOSURE_STATUS, NORMAL_CLOSURE_REASON)
            _connectionState.value = ConnectionState.Disconnected
        }

        private fun createListener(): WebSocketListener {
            return object : WebSocketListener() {
                override fun onOpen(
                    webSocket: WebSocket,
                    response: Response,
                ) {
                    this@OkHttpWebSocketClient.webSocket = webSocket
                    _connectionState.value = ConnectionState.Connected
                }

                override fun onMessage(
                    webSocket: WebSocket,
                    text: String,
                ) {
                    _messages.tryEmit(text)
                }

                override fun onMessage(
                    webSocket: WebSocket,
                    bytes: ByteString,
                ) {
                    _messages.tryEmit(bytes.utf8())
                }

                override fun onClosing(
                    webSocket: WebSocket,
                    code: Int,
                    reason: String,
                ) {
                    this@OkHttpWebSocketClient.webSocket = null
                    _connectionState.value = ConnectionState.Disconnected
                    webSocket.close(code, reason)
                }

                override fun onClosed(
                    webSocket: WebSocket,
                    code: Int,
                    reason: String,
                ) {
                    this@OkHttpWebSocketClient.webSocket = null
                    _connectionState.value = ConnectionState.Disconnected
                }

                override fun onFailure(
                    webSocket: WebSocket,
                    t: Throwable,
                    response: Response?,
                ) {
                    this@OkHttpWebSocketClient.webSocket = null
                    _connectionState.value = ConnectionState.Error(t.message)
                }
            }
        }

        private companion object {
            const val NORMAL_CLOSURE_STATUS = 1000
            const val NORMAL_CLOSURE_REASON = "Normal closure"
        }
    }
