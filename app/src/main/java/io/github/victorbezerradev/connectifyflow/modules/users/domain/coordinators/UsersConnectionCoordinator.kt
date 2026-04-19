package io.github.victorbezerradev.connectifyflow.modules.users.domain.coordinators

import android.util.Log
import io.github.victorbezerradev.connectifyflow.core.websocket.ConnectionState
import io.github.victorbezerradev.connectifyflow.core.websocket.WebSocketClient
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.states.CommunicationStatusState
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.states.UsersUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsersConnectionCoordinator
    @Inject
    constructor(
        private val webSocketClient: WebSocketClient,
        private val heartbeatCoordinator: HeartbeatCoordinator,
    ) {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

        private val _uiState = MutableStateFlow(UsersUiState())
        val uiState: StateFlow<UsersUiState> = _uiState.asStateFlow()

        private var connectionJob: Job? = null
        private var started = false

        fun start() {
            if (started) return
            started = true

            observeConnection()
            observeHeartbeat()
            connectIfNeeded()
        }

        fun stop() {
            connectionJob?.cancel()
            heartbeatCoordinator.stop()
            webSocketClient.disconnect()
            started = false
        }

        private fun observeConnection() {
            if (connectionJob != null) return

            connectionJob =
                scope.launch {
                    webSocketClient.connectionState.collect { state ->
                        Log.d(TAG, "Connection state: $state")

                        updateState { copy(connectionState = state) }

                        when (state) {
                            is ConnectionState.Connected -> {
                                heartbeatCoordinator.start()
                            }

                            is ConnectionState.Disconnected -> {
                                heartbeatCoordinator.stop()
                                updateCommunicationStatus(CommunicationStatusState.Idle)
                                reconnect()
                            }

                            is ConnectionState.Error -> {
                                heartbeatCoordinator.stop()

                                val msg =
                                    state.message?.takeIf { it.isNotBlank() }
                                        ?: "WebSocket error"

                                updateCommunicationStatus(
                                    CommunicationStatusState.Error(msg),
                                )

                                reconnect()
                            }

                            else -> Unit
                        }
                    }
                }
        }

        private fun observeHeartbeat() {
            scope.launch {
                heartbeatCoordinator.communicationStatus.collect {
                    updateCommunicationStatus(it)
                }
            }

            scope.launch {
                heartbeatCoordinator.countdown.collect { seconds ->
                    updateState { copy(heartbeatCountdown = seconds) }
                }
            }
        }

        private fun connectIfNeeded() {
            when (uiState.value.connectionState) {
                is ConnectionState.Connected,
                is ConnectionState.Connecting,
                -> return

                else -> {
                    Log.d(TAG, "Connecting websocket...")
                    webSocketClient.connect()
                }
            }
        }

        private fun reconnect() {
            scope.launch {
                delay(RECONNECT_DELAY)
                connectIfNeeded()
            }
        }

        private fun updateCommunicationStatus(status: CommunicationStatusState) {
            Log.d(TAG, "Status: $status")
            updateState { copy(communicationStatus = status) }
        }

        private fun updateState(block: UsersUiState.() -> UsersUiState) {
            _uiState.value = _uiState.value.block()
        }

        companion object {
            private const val TAG = "UsersCoordinator"
            private const val RECONNECT_DELAY = 3000L
        }
    }
