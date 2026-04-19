package io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.states

import io.github.victorbezerradev.connectifyflow.core.websocket.ConnectionState

data class UsersUiState(
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val communicationStatus: CommunicationStatusState = CommunicationStatusState.Idle,
    val heartbeatCountdown: Int = 30,
)
