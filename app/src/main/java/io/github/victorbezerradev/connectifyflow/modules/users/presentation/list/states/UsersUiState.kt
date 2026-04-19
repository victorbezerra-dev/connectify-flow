package io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.states

import io.github.victorbezerradev.connectifyflow.core.websocket.ConnectionState
import io.github.victorbezerradev.connectifyflow.modules.users.domain.models.User

data class UsersUiState(
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val communicationStatus: CommunicationStatusState = CommunicationStatusState.Idle,
    val heartbeatCountdown: Int = 30,
    val isLoading: Boolean = false,
    val users: List<User> = emptyList(),
    val errorMessage: String? = null,
)
