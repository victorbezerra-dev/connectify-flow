package io.github.victorbezerradev.connectifyflow.modules.users.domain.coordinators

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.github.victorbezerradev.connectifyflow.core.websocket.ConnectionState
import io.github.victorbezerradev.connectifyflow.core.websocket.WebSocketClient
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.states.CommunicationStatusState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class HeartbeatCoordinatorTest {
    private lateinit var webSocketClient: WebSocketClient
    private lateinit var connectionStateFlow: MutableStateFlow<ConnectionState>
    private lateinit var messagesFlow: MutableSharedFlow<String>

    @Before
    fun setUp() {
        webSocketClient = mockk(relaxed = true)
        connectionStateFlow = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
        messagesFlow = MutableSharedFlow(extraBufferCapacity = 1)

        every { webSocketClient.connectionState } returns connectionStateFlow
        every { webSocketClient.messages } returns messagesFlow
        coEvery { webSocketClient.send(any()) } returns true
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `start should run heartbeat cycle and emit expected states when connected`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)

            val coordinator =
                HeartbeatCoordinator(
                    webSocketClient = webSocketClient,
                    dispatcher = dispatcher,
                )

            connectionStateFlow.value = ConnectionState.Connected

            coordinator.communicationStatus.test {
                assertThat(awaitItem()).isEqualTo(CommunicationStatusState.Idle)

                coordinator.start()
                runCurrent()

                assertThat(awaitItem()).isEqualTo(
                    CommunicationStatusState.Sending("hello"),
                )

                advanceTimeBy(2000)
                runCurrent()

                assertThat(awaitItem()).isEqualTo(
                    CommunicationStatusState.AwaitingResponse("hello"),
                )

                messagesFlow.emit("hello")
                runCurrent()

                advanceTimeBy(1000)
                runCurrent()

                assertThat(awaitItem()).isEqualTo(
                    CommunicationStatusState.Received("hello"),
                )

                advanceTimeBy(2000)
                runCurrent()

                assertThat(awaitItem()).isEqualTo(
                    CommunicationStatusState.Idle,
                )

                coordinator.stop()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `start should emit error when send fails`() =
        runTest {
            coEvery { webSocketClient.send("hello") } returns false

            val coordinator =
                HeartbeatCoordinator(
                    webSocketClient = webSocketClient,
                    dispatcher = StandardTestDispatcher(testScheduler),
                )

            connectionStateFlow.value = ConnectionState.Connected

            coordinator.communicationStatus.test {
                assertThat(awaitItem()).isEqualTo(CommunicationStatusState.Idle)

                coordinator.start()
                runCurrent()

                assertThat(awaitItem()).isEqualTo(
                    CommunicationStatusState.Sending("hello"),
                )

                assertThat(awaitItem()).isEqualTo(
                    CommunicationStatusState.Error("Send failed"),
                )

                coordinator.stop()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `countdown should decrease every second after start`() =
        runTest {
            val coordinator =
                HeartbeatCoordinator(
                    webSocketClient = webSocketClient,
                    dispatcher = StandardTestDispatcher(testScheduler),
                )

            connectionStateFlow.value = ConnectionState.Connected

            coordinator.start()
            runCurrent()

            assertThat(coordinator.countdown.value).isEqualTo(30)

            advanceTimeBy(1000)
            runCurrent()
            assertThat(coordinator.countdown.value).isEqualTo(29)

            advanceTimeBy(1000)
            runCurrent()
            assertThat(coordinator.countdown.value).isEqualTo(28)

            advanceTimeBy(1000)
            runCurrent()
            assertThat(coordinator.countdown.value).isEqualTo(27)

            coordinator.stop()
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `stop should reset communication status and countdown`() =
        runTest {
            val coordinator =
                HeartbeatCoordinator(
                    webSocketClient = webSocketClient,
                    dispatcher = StandardTestDispatcher(testScheduler),
                )

            connectionStateFlow.value = ConnectionState.Connected

            coordinator.start()
            runCurrent()

            coordinator.stop()
            runCurrent()

            assertThat(coordinator.communicationStatus.value)
                .isEqualTo(CommunicationStatusState.Idle)

            assertThat(coordinator.countdown.value).isEqualTo(30)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `start should do nothing when websocket is disconnected`() =
        runTest {
            val coordinator =
                HeartbeatCoordinator(
                    webSocketClient = webSocketClient,
                    dispatcher = StandardTestDispatcher(testScheduler),
                )

            coordinator.communicationStatus.test {
                assertThat(awaitItem()).isEqualTo(CommunicationStatusState.Idle)

                coordinator.start()
                runCurrent()

                expectNoEvents()

                coordinator.stop()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `start should not create duplicated heartbeat loop when called twice`() =
        runTest {
            val coordinator =
                HeartbeatCoordinator(
                    webSocketClient = webSocketClient,
                    dispatcher = StandardTestDispatcher(testScheduler),
                )

            connectionStateFlow.value = ConnectionState.Connected

            coordinator.communicationStatus.test {
                assertThat(awaitItem()).isEqualTo(CommunicationStatusState.Idle)

                coordinator.start()
                coordinator.start()
                runCurrent()

                assertThat(awaitItem()).isEqualTo(
                    CommunicationStatusState.Sending("hello"),
                )

                coordinator.stop()
                cancelAndIgnoreRemainingEvents()
            }
        }
}
