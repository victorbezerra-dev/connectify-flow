package io.github.victorbezerradev.connectifyflow.modules.users.domain.coordinators

import app.cash.turbine.test
import com.google.common.base.Verify.verify
import com.google.common.truth.Truth.assertThat
import io.github.victorbezerradev.connectifyflow.core.websocket.ConnectionState
import io.github.victorbezerradev.connectifyflow.core.websocket.WebSocketClient
import io.github.victorbezerradev.connectifyflow.modules.users.domain.interfaces.HeartbeatCoordinator
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.states.CommunicationStatusState
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UsersConnectionCoordinatorTest {
    private val dispatcher = StandardTestDispatcher()

    private lateinit var webSocketClient: WebSocketClient
    private lateinit var heartbeatCoordinator: HeartbeatCoordinator
    private lateinit var coordinator: UsersConnectionCoordinator

    private lateinit var connectionState: MutableStateFlow<ConnectionState>
    private lateinit var communicationStatus: MutableStateFlow<CommunicationStatusState>
    private lateinit var countdown: MutableStateFlow<Int>

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)

        connectionState = MutableStateFlow(ConnectionState.Disconnected)
        communicationStatus = MutableStateFlow(CommunicationStatusState.Idle)
        countdown = MutableStateFlow(30)

        webSocketClient = mockk(relaxed = true)
        heartbeatCoordinator = mockk(relaxed = true)

        every { webSocketClient.connectionState } returns connectionState
        every { webSocketClient.messages } returns MutableSharedFlow()

        every { heartbeatCoordinator.communicationStatus } returns communicationStatus
        every { heartbeatCoordinator.countdown } returns countdown

        coordinator =
            UsersConnectionCoordinator(
                webSocketClient = webSocketClient,
                heartbeatCoordinator = heartbeatCoordinator,
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `start should start heartbeat and connect websocket`() =
        runTest(dispatcher) {
            coordinator.start()
            advanceUntilIdle()

            verify(exactly = 1) { heartbeatCoordinator.start() }
            verify(exactly = 1) { webSocketClient.connect() }
        }

    @Test
    fun `connected state should update ui and restart heartbeat`() =
        runTest(dispatcher) {
            coordinator.start()
            advanceUntilIdle()

            connectionState.value = ConnectionState.Connected
            advanceUntilIdle()

            assertThat(coordinator.uiState.value.connectionState)
                .isInstanceOf(ConnectionState.Connected::class.java)

            verify(exactly = 2) { heartbeatCoordinator.start() }
        }

    @Test
    fun `should update ui state when heartbeat emits new values`() =
        runTest(dispatcher) {
            coordinator.start()
            advanceUntilIdle()

            coordinator.uiState.test {
                val initial = awaitItem()
                assertThat(initial.communicationStatus).isEqualTo(CommunicationStatusState.Idle)
                assertThat(initial.heartbeatCountdown).isEqualTo(30)

                val status = CommunicationStatusState.Received("pong")

                communicationStatus.value = status
                countdown.value = 15
                advanceUntilIdle()

                val statusUpdated = awaitItem()
                assertThat(statusUpdated.communicationStatus).isEqualTo(status)
                assertThat(statusUpdated.heartbeatCountdown).isEqualTo(30)

                val countdownUpdated = awaitItem()
                assertThat(countdownUpdated.communicationStatus).isEqualTo(status)
                assertThat(countdownUpdated.heartbeatCountdown).isEqualTo(15)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `error should map to friendly message`() =
        runTest(dispatcher) {
            coordinator.start()
            advanceUntilIdle()

            connectionState.value = ConnectionState.Error("Unable to resolve host")
            advanceUntilIdle()

            assertThat(coordinator.uiState.value.communicationStatus)
                .isEqualTo(CommunicationStatusState.Error("No internet connection."))
        }

    @Test
    fun `disconnect should stop heartbeat`() =
        runTest(dispatcher) {
            coordinator.start()
            advanceUntilIdle()

            connectionState.value = ConnectionState.Connected
            advanceUntilIdle()

            connectionState.value = ConnectionState.Disconnected
            runCurrent()

            verify { heartbeatCoordinator.stop() }
        }

    @Test
    fun `should schedule reconnect after delay`() =
        runTest(dispatcher) {
            coordinator.start()
            advanceUntilIdle()

            connectionState.value = ConnectionState.Connected
            advanceUntilIdle()

            connectionState.value = ConnectionState.Disconnected
            runCurrent()

            verify(exactly = 1) { webSocketClient.connect() }

            advanceTimeBy(2999)
            runCurrent()

            verify(exactly = 1) { webSocketClient.connect() }

            advanceTimeBy(1)
            runCurrent()

            verify(exactly = 2) { webSocketClient.connect() }
        }

    @Test
    fun `stop should reset state and disconnect`() =
        runTest(dispatcher) {
            coordinator.start()
            advanceUntilIdle()

            communicationStatus.value = CommunicationStatusState.Received("pong")
            countdown.value = 10
            connectionState.value = ConnectionState.Connected

            advanceUntilIdle()

            coordinator.stop()
            advanceUntilIdle()

            verify { webSocketClient.disconnect() }
            verify { heartbeatCoordinator.stop() }

            val state = coordinator.uiState.value

            assertThat(state.connectionState)
                .isInstanceOf(ConnectionState.Disconnected::class.java)

            assertThat(state.communicationStatus)
                .isEqualTo(CommunicationStatusState.Idle)

            assertThat(state.heartbeatCountdown)
                .isEqualTo(30)
        }

    @Test
    fun `calling start twice should force reconnect`() =
        runTest(dispatcher) {
            coordinator.start()
            advanceUntilIdle()

            coordinator.start()
            advanceUntilIdle()

            assertThat(coordinator.uiState.value.connectionState)
                .isInstanceOf(ConnectionState.Connecting::class.java)

            verify(exactly = 2) { webSocketClient.connect() }
        }
}
