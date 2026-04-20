package io.github.victorbezerradev.connectifyflow.core.websocket

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString.Companion.encodeUtf8
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OkHttpWebSocketClientTest {
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var webSocket: WebSocket
    private lateinit var client: OkHttpWebSocketClient

    private val webSocketUrl = "wss://ws.postman-echo.com/raw"

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        okHttpClient = io.mockk.mockk(relaxed = true)
        webSocket = io.mockk.mockk(relaxed = true)

        client =
            OkHttpWebSocketClient(
                okHttpClient = okHttpClient,
                webSocketUrl = webSocketUrl,
            )
    }

    @Test
    fun `connect should transition to CONNECTING and initialize a new websocket session`() {
        val listenerSlot = slot<WebSocketListener>()

        every {
            okHttpClient.newWebSocket(any(), capture(listenerSlot))
        } returns webSocket

        client.connect()

        assertThat(client.connectionState.value)
            .isInstanceOf(ConnectionState.Connecting::class.java)

        verify(exactly = 1) {
            okHttpClient.newWebSocket(
                match<Request> { request ->
                    request.url.host == "ws.postman-echo.com" &&
                        request.url.encodedPath == "/raw"
                },
                any(),
            )
        }
    }

    @Test
    fun `onOpen should transition connection state to CONNECTED after successful handshake`() {
        val listenerSlot = slot<WebSocketListener>()

        every {
            okHttpClient.newWebSocket(any(), capture(listenerSlot))
        } returns webSocket

        client.connect()
        listenerSlot.captured.onOpen(webSocket, fakeResponse())

        assertThat(client.connectionState.value)
            .isEqualTo(ConnectionState.Connected)
    }

    @Test
    fun `send should dispatch message through active websocket when connection is established`() {
        val listenerSlot = slot<WebSocketListener>()

        every {
            okHttpClient.newWebSocket(any(), capture(listenerSlot))
        } returns webSocket
        every { webSocket.send("hello") } returns true

        client.connect()
        listenerSlot.captured.onOpen(webSocket, fakeResponse())

        val result = client.send("hello")

        assertThat(result).isTrue()
        verify(exactly = 1) { webSocket.send("hello") }
    }

    @Test
    fun `onMessage text should propagate incoming messages to observers`() =
        runTest {
            val listenerSlot = slot<WebSocketListener>()

            every {
                okHttpClient.newWebSocket(any(), capture(listenerSlot))
            } returns webSocket

            client.messages.test {
                client.connect()
                listenerSlot.captured.onMessage(webSocket, "hello")

                assertThat(awaitItem()).isEqualTo("hello")
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onMessage binary should decode payload as UTF-8 and propagate to observers`() =
        runTest {
            val listenerSlot = slot<WebSocketListener>()

            every {
                okHttpClient.newWebSocket(any(), capture(listenerSlot))
            } returns webSocket

            client.messages.test {
                client.connect()
                listenerSlot.captured.onMessage(
                    webSocket,
                    "hello-bytes".encodeUtf8(),
                )

                assertThat(awaitItem()).isEqualTo("hello-bytes")
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `disconnect should gracefully close websocket and transition state to DISCONNECTED`() {
        val listenerSlot = slot<WebSocketListener>()

        every {
            okHttpClient.newWebSocket(any(), capture(listenerSlot))
        } returns webSocket
        every { webSocket.close(1000, "Normal closure") } returns true

        client.connect()
        listenerSlot.captured.onOpen(webSocket, fakeResponse())

        client.disconnect()

        assertThat(client.connectionState.value)
            .isEqualTo(ConnectionState.Disconnected)

        verify(exactly = 1) {
            webSocket.close(1000, "Normal closure")
        }
    }

    @Test
    fun `send should fail fast when no active connection is available`() {
        val result = client.send("hello")

        assertThat(result).isFalse()
    }

    @Test
    fun `connect should be idempotent when already in CONNECTED state`() {
        val listenerSlot = slot<WebSocketListener>()

        every {
            okHttpClient.newWebSocket(any(), capture(listenerSlot))
        } returns webSocket

        client.connect()
        listenerSlot.captured.onOpen(webSocket, fakeResponse())

        client.connect()

        verify(exactly = 1) {
            okHttpClient.newWebSocket(any(), any())
        }
    }

    @Test
    fun `connect should cancel existing websocket before establishing a new connection`() {
        val firstSocket = io.mockk.mockk<WebSocket>(relaxed = true)
        val secondSocket = io.mockk.mockk<WebSocket>(relaxed = true)

        every { firstSocket.cancel() } just Runs
        every { secondSocket.cancel() } just Runs

        every {
            okHttpClient.newWebSocket(any(), any())
        } returnsMany listOf(firstSocket, secondSocket)

        client.connect()
        client.connect()

        verify(exactly = 1) { firstSocket.cancel() }
        verify(exactly = 2) { okHttpClient.newWebSocket(any(), any()) }
    }

    @Test
    fun `onClosing should trigger graceful shutdown and transition to DISCONNECTED`() {
        val listenerSlot = slot<WebSocketListener>()

        every {
            okHttpClient.newWebSocket(any(), capture(listenerSlot))
        } returns webSocket
        every { webSocket.close(1000, "bye") } returns true

        client.connect()
        listenerSlot.captured.onClosing(webSocket, 1000, "bye")

        assertThat(client.connectionState.value)
            .isEqualTo(ConnectionState.Disconnected)

        verify(exactly = 1) { webSocket.close(1000, "bye") }
    }

    @Test
    fun `onClosed should ensure state consistency by transitioning to DISCONNECTED`() {
        val listenerSlot = slot<WebSocketListener>()

        every {
            okHttpClient.newWebSocket(any(), capture(listenerSlot))
        } returns webSocket

        client.connect()
        listenerSlot.captured.onClosed(webSocket, 1000, "closed")

        assertThat(client.connectionState.value)
            .isEqualTo(ConnectionState.Disconnected)
    }

    @Test
    fun `onFailure should transition to ERROR state and expose failure reason`() {
        val listenerSlot = slot<WebSocketListener>()

        every {
            okHttpClient.newWebSocket(any(), capture(listenerSlot))
        } returns webSocket

        client.connect()
        listenerSlot.captured.onFailure(
            webSocket,
            RuntimeException("network error"),
            null,
        )

        assertThat(client.connectionState.value)
            .isEqualTo(ConnectionState.Error("network error"))
    }

    private fun fakeResponse(): Response =
        Response.Builder()
            .request(
                Request.Builder()
                    .url("https://ws.postman-echo.com/raw")
                    .build(),
            )
            .protocol(Protocol.HTTP_1_1)
            .code(101)
            .message("Switching Protocols")
            .build()
}
