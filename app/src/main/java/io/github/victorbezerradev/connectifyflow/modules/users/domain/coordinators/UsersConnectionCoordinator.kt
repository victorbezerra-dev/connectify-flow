package io.github.victorbezerradev.connectifyflow.modules.users.domain.coordinators

import android.util.Log
import io.github.victorbezerradev.connectifyflow.core.websocket.ConnectionState
import io.github.victorbezerradev.connectifyflow.core.websocket.WebSocketClient
import io.github.victorbezerradev.connectifyflow.modules.users.domain.interfaces.HeartbeatCoordinator
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
import kotlinx.coroutines.flow.collect
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
        private var reconnectionAttempts = 0
        private var hasObservedInitialConnectionState = false
        private var lastConnectionState: ConnectionState? = null

        fun start() {
            if (started) {
                Log.i(TAG, "Coordinator already started, forcing reconnect")
                reconnectionAttempts = 0
                cancelReconnect()

                updateState {
                    copy(
                        connectionState = ConnectionState.Connecting,
                        communicationStatus = CommunicationStatusState.Idle,
                    )
                }

                webSocketClient.connect()
                return
            }

            started = true
            reconnectionAttempts = 0
            hasObservedInitialConnectionState = false
            lastConnectionState = null

            Log.i(TAG, "Starting coordinator")

            observeConnection()
            observeHeartbeat()
            heartbeatCoordinator.start()

            updateState {
                copy(
                    connectionState = ConnectionState.Connecting,
                    communicationStatus = CommunicationStatusState.Idle,
                )
            }

            webSocketClient.connect()
        }

        fun stop() {
            if (!started) {
                Log.w(TAG, "Coordinator already stopped, ignoring stop()")
                return
            }

            Log.i(TAG, "Stopping coordinator")
            started = false

            cancelReconnect()
            cancelObservers()

            heartbeatCoordinator.stop()
            webSocketClient.disconnect()

            reconnectionAttempts = 0
            hasObservedInitialConnectionState = false
            lastConnectionState = null

            updateState {
                copy(
                    connectionState = ConnectionState.Disconnected,
                    communicationStatus = CommunicationStatusState.Idle,
                    heartbeatCountdown = 30,
                )
            }
        }

        fun retryConnection() {
            if (!started) return

            Log.i(TAG, "Manual retry requested by user")
            reconnectionAttempts = 0
            cancelReconnect()
            heartbeatCoordinator.stop()

            updateState {
                copy(
                    connectionState = ConnectionState.Connecting,
                    communicationStatus = CommunicationStatusState.Idle,
                )
            }

            webSocketClient.connect()
        }

        private fun observeConnection() {
            if (connectionJob?.isActive == true) return

            connectionJob =
                scope.launch {
                    webSocketClient.connectionState.collect { state ->
                        if (!started) return@collect
                        if (shouldIgnoreInitialEmission(state)) return@collect

                        Log.d(TAG, "Connection event: $state")
                        handleConnectionState(state)
                        lastConnectionState = state
                    }
                }
        }

        private fun shouldIgnoreInitialEmission(state: ConnectionState): Boolean {
            if (hasObservedInitialConnectionState) return false

            hasObservedInitialConnectionState = true
            lastConnectionState = state

            Log.d(TAG, "Ignoring initial connection state emission: $state")

            if (state is ConnectionState.Connected) {
                updateState { copy(connectionState = state) }
            }

            return true
        }

        private fun handleConnectionState(state: ConnectionState) {
            when (state) {
                is ConnectionState.Connected -> handleConnected()
                is ConnectionState.Connecting -> handleConnecting()
                is ConnectionState.Disconnected -> handleDisconnected()
                is ConnectionState.Error -> handleError(state)
            }
        }

        private fun handleConnected() {
            reconnectionAttempts = 0
            cancelReconnect()

            updateState {
                copy(
                    connectionState = ConnectionState.Connected,
                    communicationStatus = CommunicationStatusState.Idle,
                )
            }

            val wasNotConnectedBefore = lastConnectionState !is ConnectionState.Connected
            if (wasNotConnectedBefore) {
                heartbeatCoordinator.start()
            }
        }

        private fun handleConnecting() {
            updateState {
                copy(connectionState = ConnectionState.Connecting)
            }
        }

        private fun handleDisconnected() {
            heartbeatCoordinator.stop()

            updateState {
                copy(communicationStatus = CommunicationStatusState.Idle)
            }

            if (canReconnect()) {
                updateState {
                    copy(connectionState = ConnectionState.Connecting)
                }
                scheduleReconnect()
            } else {
                Log.w(TAG, "Reconnect limit reached. Waiting for manual retry.")
                updateState {
                    copy(connectionState = ConnectionState.Disconnected)
                }
            }
        }

        private fun handleError(state: ConnectionState.Error) {
            heartbeatCoordinator.stop()

            val friendlyMessage = mapFriendlyErrorMessage(state.message)

            if (canReconnect()) {
                updateState {
                    copy(
                        connectionState = ConnectionState.Connecting,
                        communicationStatus = CommunicationStatusState.Error(friendlyMessage),
                    )
                }
                scheduleReconnect()
            } else {
                Log.w(TAG, "Reconnect limit reached after error. Waiting for manual retry.")
                updateState {
                    copy(
                        connectionState = ConnectionState.Disconnected,
                        communicationStatus = CommunicationStatusState.Error(friendlyMessage),
                    )
                }
            }
        }

        private fun canReconnect(): Boolean = reconnectionAttempts < MAX_RECONNECT_ATTEMPTS

        private fun mapFriendlyErrorMessage(message: String?): String {
            val technicalMessage = message.orEmpty()

            return when {
                technicalMessage.contains("Unable to resolve host", ignoreCase = true) ->
                    "No internet connection."
                technicalMessage.contains("Failed to connect", ignoreCase = true) ||
                    technicalMessage.contains("Connection refused", ignoreCase = true) ->
                    "Server is unreachable."
                else -> "Connection failed. Please try again."
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

        private fun scheduleReconnect() {
            val shouldSchedule =
                started &&
                    reconnectJob?.isActive != true &&
                    reconnectionAttempts < MAX_RECONNECT_ATTEMPTS

            if (!shouldSchedule) return

            reconnectJob =
                scope.launch {
                    reconnectionAttempts++

                    Log.d(
                        TAG,
                        "Scheduling reconnect attempt #$reconnectionAttempts in ${RECONNECT_DELAY}ms",
                    )

                    delay(RECONNECT_DELAY)

                    if (started) {
                        updateState {
                            copy(connectionState = ConnectionState.Connecting)
                        }
                        webSocketClient.connect()
                    }

                    reconnectJob = null
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
        }

        private fun updateState(block: UsersUiState.() -> UsersUiState) {
            _uiState.update { current -> current.block() }
        }

        companion object {
            private const val TAG = "UsersCoordinator"
            private const val RECONNECT_DELAY = 3000L
            private const val MAX_RECONNECT_ATTEMPTS = 3
        }
    }
