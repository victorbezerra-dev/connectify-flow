package io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.states

sealed interface CommunicationStatusState {
    data object Idle : CommunicationStatusState

    data class Sending(val message: String) : CommunicationStatusState

    data class AwaitingResponse(val message: String) : CommunicationStatusState

    data class Received(val message: String) : CommunicationStatusState

    data class Error(val reason: String) : CommunicationStatusState
}
