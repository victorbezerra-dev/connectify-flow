package io.github.victorbezerradev.connectifyflow.modules.users.presentation.list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.victorbezerradev.connectifyflow.core.websocket.ConnectionState
import io.github.victorbezerradev.connectifyflow.core.websocket.WebSocketClient
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.actions.UsersUiAction
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.states.CommunicationStatusState
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.states.UsersUiState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class UsersListViewModel
    @Inject
    constructor(
        private val webSocketClient: WebSocketClient,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(UsersUiState())
        val uiState = _uiState.asStateFlow()

        private var messagesJob: Job? = null
        private var connectionJob: Job? = null
        private var heartbeatJob: Job? = null

        private var started = false
        private var pendingHeartbeatResponse: CompletableDeferred<String>? = null

        fun onAction(action: UsersUiAction) {
            when (action) {
                UsersUiAction.ScreenStarted -> start()
            }
        }

        private fun start() {
            if (started) return
            started = true

            observeMessages()
            observeConnectionState()
            connectIfNeeded()
        }

        private fun connectIfNeeded() {
            when (uiState.value.connectionState) {
                is ConnectionState.Connected,
                is ConnectionState.Connecting,
                -> return

                else -> {
                    Log.d(TAG, "Connecting websocket")
                    webSocketClient.connect()
                }
            }
        }

        private fun observeMessages() {
            if (messagesJob != null) return

            messagesJob =
                viewModelScope.launch {
                    webSocketClient.messages.collect { message ->
                        Log.d(TAG, "Message received: $message")

                        val deferred = pendingHeartbeatResponse

                        if (deferred != null && !deferred.isCompleted) {
                            deferred.complete(message)
                        } else {
                            Log.d(TAG, "No heartbeat waiting for this message. Ignoring visual transition.")
                        }
                    }
                }
        }

        private fun observeConnectionState() {
            if (connectionJob != null) return

            connectionJob =
                viewModelScope.launch {
                    webSocketClient.connectionState.collect { state ->
                        Log.d(TAG, "Connection state changed: $state")

                        updateState {
                            copy(connectionState = state)
                        }

                        when (state) {
                            is ConnectionState.Connected -> {
                                startHeartbeat()
                            }

                            is ConnectionState.Connecting -> Unit

                            is ConnectionState.Disconnected -> {
                                stopHeartbeat()
                                pendingHeartbeatResponse?.cancel()
                                pendingHeartbeatResponse = null
                                updateCommunicationStatus(CommunicationStatusState.Idle)
                                reconnectWithDelay()
                            }

                            is ConnectionState.Error -> {
                                stopHeartbeat()
                                pendingHeartbeatResponse?.cancel()
                                pendingHeartbeatResponse = null

                                val errorMessage =
                                    state.message
                                        ?.takeIf { it.isNotBlank() }
                                        ?: "WebSocket connection error"

                                updateCommunicationStatus(
                                    CommunicationStatusState.Error(errorMessage),
                                )

                                reconnectWithDelay()
                            }
                        }
                    }
                }
        }

        private fun reconnectWithDelay() {
            viewModelScope.launch {
                delay(RECONNECT_DELAY_MILLIS)
                connectIfNeeded()
            }
        }

        private fun startHeartbeat() {
            if (heartbeatJob?.isActive == true) return

            heartbeatJob =
                viewModelScope.launch {
                    Log.d(TAG, "Heartbeat started")

                    while (isActive) {
                        if (uiState.value.connectionState is ConnectionState.Connected) {
                            launch { runHeartbeatCycle() }
                        }

                        for (i in 30 downTo 1) {
                            updateState { copy(heartbeatCountdown = i) }
                            delay(1000)
                        }
                    }
                }
        }

        private suspend fun runHeartbeatCycle() {
            val message = HEARTBEAT_MESSAGE
            val responseDeferred = CompletableDeferred<String>()
            pendingHeartbeatResponse = responseDeferred

            updateCommunicationStatus(
                CommunicationStatusState.Sending(message),
            )

            val sent = webSocketClient.send(message)

            if (!sent) {
                pendingHeartbeatResponse = null
                updateCommunicationStatus(
                    CommunicationStatusState.Error("Failed to send heartbeat"),
                )
                return
            }

            delay(SENDING_TO_AWAITING_DELAY_MILLIS)

            updateCommunicationStatus(
                CommunicationStatusState.AwaitingResponse(message),
            )

            val response =
                try {
                    responseDeferred.await()
                } catch (exception: CancellationException) {
                    Log.e(TAG, "Waiting response failed", exception)
                    pendingHeartbeatResponse = null
                    updateCommunicationStatus(
                        CommunicationStatusState.Error("Response wait cancelled"),
                    )
                    return
                }

            delay(AWAITING_TO_RECEIVED_DELAY_MILLIS)

            updateCommunicationStatus(
                CommunicationStatusState.Received(response),
            )

            delay(RECEIVED_TO_IDLE_DELAY_MILLIS)

            updateCommunicationStatus(
                CommunicationStatusState.Idle,
            )

            pendingHeartbeatResponse = null
        }

        private fun stopHeartbeat() {
            heartbeatJob?.cancel()
            heartbeatJob = null
        }

        private fun updateCommunicationStatus(status: CommunicationStatusState) {
            Log.d(TAG, "Communication status changed: $status")
            updateState {
                copy(communicationStatus = status)
            }
        }

        private fun updateState(reducer: UsersUiState.() -> UsersUiState) {
            _uiState.value = _uiState.value.reducer()
        }

        override fun onCleared() {
            stopHeartbeat()
            pendingHeartbeatResponse?.cancel()
            messagesJob?.cancel()
            connectionJob?.cancel()
            webSocketClient.disconnect()
            super.onCleared()
        }

        companion object {
            private const val TAG = "UsersViewModel"
            private const val HEARTBEAT_MESSAGE = "hello"
            private const val RECONNECT_DELAY_MILLIS = 3_000L
            private const val SENDING_TO_AWAITING_DELAY_MILLIS = 2_000L
            private const val AWAITING_TO_RECEIVED_DELAY_MILLIS = 1_000L
            private const val RECEIVED_TO_IDLE_DELAY_MILLIS = 2_000L
        }
    }
