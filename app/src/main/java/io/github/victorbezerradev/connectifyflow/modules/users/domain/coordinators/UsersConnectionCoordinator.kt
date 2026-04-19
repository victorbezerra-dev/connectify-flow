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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

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
        private var communicationStatusJob: Job? = null
        private var countdownJob: Job? = null
        private var reconnectJob: Job? = null

        private var started = false

        fun start() {
            if (started) {
                Log.w(TAG, "Coordinator already started, ignoring start()")
                return
            }

            started = true
            Log.i(TAG, "Starting coordinator")

            observeConnection()
            observeHeartbeat()

            heartbeatCoordinator.start()
            connectIfNeeded()
        }

        fun stop() {
            if (!started) {
                Log.w(TAG, "Coordinator already stopped, ignoring stop()")
                return
            }

            Log.i(TAG, "Stopping coordinator (cleaning up connection)")
            started = false

            cancelObservers()
            heartbeatCoordinator.stop()
            webSocketClient.disconnect()

            updateState {
                copy(
                    connectionState = ConnectionState.Disconnected,
                    communicationStatus = CommunicationStatusState.Idle,
                    heartbeatCountdown = 30,
                )
            }
        }

        private fun observeConnection() {
            if (connectionJob?.isActive == true) return

            connectionJob =
                scope.launch {
                    webSocketClient.connectionState.collect { state ->
                        if (!started) return@collect
                        Log.d(TAG, "Connection event: $state")

                        when (state) {
                            is ConnectionState.Connected -> {
                                updateState { copy(connectionState = state) }
                                cancelReconnect()
                                heartbeatCoordinator.start()
                            }

                            is ConnectionState.Disconnected -> {
                                heartbeatCoordinator.stop()
                                updateState {
                                    copy(
                                        connectionState = state,
                                        communicationStatus = CommunicationStatusState.Idle,
                                    )
                                }

                                if (started) {
                                    Log.d(TAG, "Unexpected disconnect, reconnecting...")
                                    connectIfNeeded()
                                }
                                scheduleReconnect()
                            }

                            is ConnectionState.Error -> {
                                heartbeatCoordinator.stop()
                                val message = state.message?.takeIf { it.isNotBlank() } ?: "WebSocket error"

                                updateState {
                                    copy(
                                        connectionState = state,
                                        communicationStatus = CommunicationStatusState.Error(message),
                                    )
                                }

                                if (started) connectIfNeeded()
                                scheduleReconnect()
                            }

                            is ConnectionState.Connecting -> {
                                updateState { copy(connectionState = state) }
                            }
                        }
                    }
                }
        }

        private fun observeHeartbeat() {
            if (communicationStatusJob?.isActive != true) {
                communicationStatusJob =
                    scope.launch {
                        heartbeatCoordinator.communicationStatus.collect { status ->
                            if (!started) return@collect
                            updateState { copy(communicationStatus = status) }
                        }
                    }
            }

            if (countdownJob?.isActive != true) {
                countdownJob =
                    scope.launch {
                        heartbeatCoordinator.countdown.collect { seconds ->
                            if (!started) return@collect
                            updateState { copy(heartbeatCountdown = seconds) }
                        }
                    }
            }
        }

        private fun connectIfNeeded() {
            if (!started) return

            when (webSocketClient.connectionState.value) {
                is ConnectionState.Connected,
                is ConnectionState.Connecting,
                -> return
                else -> webSocketClient.connect()
            }
        }

        private fun scheduleReconnect() {
            if (!started || reconnectJob?.isActive == true) return

            reconnectJob =
                scope.launch {
                    delay(RECONNECT_DELAY)
                    if (started) connectIfNeeded()
                }
        }

        private fun cancelReconnect() {
            reconnectJob?.cancel()
            reconnectJob = null
        }

        private fun cancelObservers() {
            connectionJob?.cancel()
            communicationStatusJob?.cancel()
            countdownJob?.cancel()
            connectionJob = null
            communicationStatusJob = null
            countdownJob = null
            cancelReconnect()
        }

        private fun updateState(block: UsersUiState.() -> UsersUiState) {
            _uiState.update { currentState -> currentState.block() }
        }

        companion object {
            private const val TAG = "UsersCoordinator"
            private const val RECONNECT_DELAY = 3000L
        }
    }
