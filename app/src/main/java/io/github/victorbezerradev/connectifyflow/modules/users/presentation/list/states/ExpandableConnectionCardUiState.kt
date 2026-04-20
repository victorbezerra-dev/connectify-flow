package io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.states

import io.github.victorbezerradev.connectifyflow.core.websocket.ConnectionState

data class ExpandableConnectionCardUiState(
    val title: String,
    val status: ConnectionState,
    val communicationStatus: CommunicationStatusState,
    val heartbeatCountdown: Int,
)
