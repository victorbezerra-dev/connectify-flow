package io.github.victorbezerradev.connectifyflow.modules.users.domain.coordinators

import android.util.Log
import io.github.victorbezerradev.connectifyflow.core.websocket.ConnectionState
import io.github.victorbezerradev.connectifyflow.core.websocket.WebSocketClient
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.states.CommunicationStatusState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class HeartbeatCoordinator
    @Inject
    constructor(
        private val webSocketClient: WebSocketClient,
    ) {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        private val communicationStatusState =
            MutableStateFlow<CommunicationStatusState>(CommunicationStatusState.Idle)
        val communicationStatus = communicationStatusState.asStateFlow()

        private val countdownState = MutableStateFlow(30)
        val countdown = countdownState.asStateFlow()

        private var job: Job? = null
        private var messagesJob: Job? = null

        private var cycleJob: Job? = null

        private var pendingResponse: CompletableDeferred<String>? = null

        fun start() {
            if (job?.isActive == true) return

            observeMessages()

            job =
                scope.launch {
                    Log.d(TAG, "Heartbeat started")

                    while (isActive) {
                        if (webSocketClient.connectionState.value is ConnectionState.Connected) {
                            if (cycleJob?.isActive != true) {
                                cycleJob =
                                    launch {
                                        runCycle()
                                    }
                            }
                        }

                        for (i in 30 downTo 1) {
                            countdownState.value = i
                            delay(1000)
                        }
                    }
                }
        }

        fun stop() {
            job?.cancel()
            job = null

            messagesJob?.cancel()
            messagesJob = null

            pendingResponse?.cancel()
            pendingResponse = null

            communicationStatusState.value = CommunicationStatusState.Idle
            countdownState.value = 30
        }

        private fun observeMessages() {
            if (messagesJob?.isActive == true) return

            messagesJob =
                scope.launch {
                    webSocketClient.messages.collect { message ->
                        val deferred = pendingResponse
                        if (deferred != null && !deferred.isCompleted) {
                            deferred.complete(message)
                        }
                    }
                }
        }

        private suspend fun runCycle() {
            val message = "hello"
            val deferred = CompletableDeferred<String>()
            pendingResponse = deferred

            communicationStatusState.value = CommunicationStatusState.Sending(message)

            val sent = webSocketClient.send(message)

            if (!sent) {
                communicationStatusState.value = CommunicationStatusState.Error("Send failed")
                pendingResponse = null
                return
            }

            delay(2000)

            communicationStatusState.value = CommunicationStatusState.AwaitingResponse(message)

            val response =
                try {
                    deferred.await()
                } catch (e: CancellationException) {
                    Log.e(TAG, "Heartbeat response wait was cancelled", e)
                    communicationStatusState.value = CommunicationStatusState.Error("Cancelled")
                    pendingResponse = null
                    return
                }

            delay(1000)

            communicationStatusState.value = CommunicationStatusState.Received(response)

            delay(2000)

            communicationStatusState.value = CommunicationStatusState.Idle
            pendingResponse = null
        }

        companion object {
            private const val TAG = "HeartbeatCoordinator"
        }
    }
