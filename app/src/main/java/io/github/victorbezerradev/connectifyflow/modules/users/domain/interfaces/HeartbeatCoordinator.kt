package io.github.victorbezerradev.connectifyflow.modules.users.domain.interfaces

import io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.states.CommunicationStatusState
import kotlinx.coroutines.flow.StateFlow

interface HeartbeatCoordinator {
    val communicationStatus: StateFlow<CommunicationStatusState>
    val countdown: StateFlow<Int>

    fun start()

    fun stop()
}
