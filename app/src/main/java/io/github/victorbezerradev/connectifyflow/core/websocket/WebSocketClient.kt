package io.github.victorbezerradev.connectifyflow.core.websocket

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface WebSocketClient {
    val messages: Flow<String>
    val connectionState: StateFlow<ConnectionState>

    fun send(message: String): Boolean

    fun disconnect()
}
