package io.github.victorbezerradev.connectifyflow.core.websocket

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
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
        private val okHttpClient: OkHttpClient,
    ) : WebSocketClient {
        private val _connectionState =
            MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
        override val connectionState: StateFlow<ConnectionState> = _connectionState

        private var webSocket: WebSocket? = null

        override val messages: Flow<String> =
            callbackFlow {
                if (webSocket != null) {
                    close(IllegalStateException("WebSocket connection is already active"))
                    return@callbackFlow
                }

                _connectionState.value = ConnectionState.Connecting

                val request =
                    Request.Builder()
                        .url("wss://ws.postman-echo.com/raw")
                        .build()

                val listener =
                    object : WebSocketListener() {
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
                            trySend(text)
                        }

                        override fun onMessage(
                            webSocket: WebSocket,
                            bytes: ByteString,
                        ) {
                            trySend(bytes.utf8())
                        }

                        override fun onClosing(
                            webSocket: WebSocket,
                            code: Int,
                            reason: String,
                        ) {
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
                            close()
                        }

                        override fun onFailure(
                            webSocket: WebSocket,
                            t: Throwable,
                            response: Response?,
                        ) {
                            this@OkHttpWebSocketClient.webSocket = null
                            _connectionState.value = ConnectionState.Error(t.message)
                            close(t)
                        }
                    }

                webSocket = okHttpClient.newWebSocket(request, listener)

                awaitClose {
                    webSocket?.close(1000, "Normal closure")
                    webSocket = null
                    _connectionState.value = ConnectionState.Disconnected
                }
            }.distinctUntilChanged()

        override fun send(message: String): Boolean {
            return webSocket?.send(message) ?: false
        }

        override fun disconnect() {
            webSocket?.close(1000, "Normal closure")
            webSocket = null
            _connectionState.value = ConnectionState.Disconnected
        }
    }
