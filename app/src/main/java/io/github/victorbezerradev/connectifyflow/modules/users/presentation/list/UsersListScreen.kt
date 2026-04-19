package io.github.victorbezerradev.connectifyflow.modules.users.presentation.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.victorbezerradev.connectifyflow.core.websocket.ConnectionState
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.actions.UsersUiAction
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.components.ExpandableConnectionCard
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.states.CommunicationStatusState

@Composable
fun UsersListScreen(viewModel: UsersListViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onAction(UsersUiAction.ScreenStarted)
    }

    UsersListContent(
        connectionState = uiState.connectionState,
        communicationStatus = uiState.communicationStatus,
        heartbeatCountdown = uiState.heartbeatCountdown,
    )
}

@Composable
private fun UsersListContent(
    connectionState: ConnectionState,
    communicationStatus: CommunicationStatusState,
    heartbeatCountdown: Int,
    modifier: Modifier = Modifier,
) {
    val statusBarPadding =
        WindowInsets.statusBars
            .asPaddingValues()
            .calculateTopPadding()

    Scaffold(
        modifier = modifier,
        topBar = {
            ExpandableConnectionCard(
                modifier =
                    Modifier.padding(
                        top = statusBarPadding + 16.dp,
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp,
                    ),
                title = "Monitor",
                status = connectionState,
                communicationStatus = communicationStatus,
                heartbeatCountdown = heartbeatCountdown,
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Users List",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UsersListContentPreviewConnected() {
    MaterialTheme {
        UsersListContent(
            connectionState = ConnectionState.Connected,
            communicationStatus = CommunicationStatusState.Received("pong"),
            heartbeatCountdown = 15,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun UsersListContentPreviewConnecting() {
    MaterialTheme {
        UsersListContent(
            connectionState = ConnectionState.Connecting,
            communicationStatus = CommunicationStatusState.AwaitingResponse("hello"),
            heartbeatCountdown = 28,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun UsersListContentPreviewError() {
    MaterialTheme {
        UsersListContent(
            connectionState = ConnectionState.Disconnected,
            communicationStatus = CommunicationStatusState.Error("Failed to reconnect"),
            heartbeatCountdown = 0,
        )
    }
}
