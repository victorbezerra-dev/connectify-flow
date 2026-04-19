package io.github.victorbezerradev.connectifyflow.modules.users.presentation.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import io.github.victorbezerradev.connectifyflow.modules.users.domain.models.User
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.actions.UsersUiAction
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.components.ExpandableConnectionCard
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.components.UserCard
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.states.CommunicationStatusState
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.states.UsersUiState
import kotlin.collections.emptyList

@Composable
fun UsersListScreen(viewModel: UsersListViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onAction(UsersUiAction.ScreenStarted)
    }

    UsersListContent(
        uiState = uiState,
        onRetry = viewModel::loadUsers,
        onAction = viewModel::onAction,
    )
}

@Composable
fun UsersListContent(
    uiState: UsersUiState,
    onRetry: () -> Unit,
    onAction: (UsersUiAction) -> Unit,
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
                status = uiState.connectionState,
                communicationStatus = uiState.communicationStatus,
                heartbeatCountdown = uiState.heartbeatCountdown,
            )
        },
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                LoadingContent(paddingValues = paddingValues)
            }

            uiState.errorMessage != null -> {
                ErrorContent(
                    paddingValues = paddingValues,
                    errorMessage = uiState.errorMessage,
                    onRetry = onRetry,
                )
            }

            else -> {
                UsersListLoadedContent(
                    paddingValues = paddingValues,
                    users = uiState.users,
                    onAction = onAction,
                )
            }
        }
    }
}

@Composable
fun LoadingContent(paddingValues: PaddingValues) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(paddingValues),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorContent(
    paddingValues: PaddingValues,
    errorMessage: String,
    onRetry: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Error loading users",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
            )

            Button(onClick = onRetry) {
                Text(text = "Try again")
            }
        }
    }
}

@Composable
fun UsersListLoadedContent(
    paddingValues: PaddingValues,
    users: List<User>,
    onAction: (UsersUiAction) -> Unit,
) {
    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(paddingValues),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Users List",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }

        items(
            items = users,
            key = { user -> user.id },
        ) { user ->
            UserCard(
                user = user,
                onProfileClick = { onAction(UsersUiAction.OpenProfileClicked(user)) },
                onWebPageClick = { url -> onAction(UsersUiAction.OpenWebPageClicked(url)) },
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UsersListContentPreviewConnected() {
    MaterialTheme {
        UsersListContent(
            uiState =
                UsersUiState(
                    connectionState = ConnectionState.Connected,
                    communicationStatus = CommunicationStatusState.Received("pong"),
                    heartbeatCountdown = 15,
                    isLoading = false,
                    users =
                        listOf(
                            User(
                                id = "1",
                                name = "João Victor",
                                email = "joao@email.com",
                                phone = "+55 69 99999-9999",
                                status = "active",
                                profileImageUrl = null,
                                profileLinkUrl = null,
                            ),
                            User(
                                id = "2",
                                name = "Maria Clara",
                                email = "maria@email.com",
                                phone = null,
                                status = "active",
                                profileImageUrl = null,
                                profileLinkUrl = "https://example.com",
                            ),
                        ),
                    errorMessage = null,
                ),
            onRetry = {},
            onAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun UsersListContentPreviewLoading() {
    MaterialTheme {
        UsersListContent(
            uiState =
                UsersUiState(
                    connectionState = ConnectionState.Connecting,
                    communicationStatus = CommunicationStatusState.AwaitingResponse("hello"),
                    heartbeatCountdown = 28,
                    isLoading = true,
                    users = emptyList(),
                    errorMessage = null,
                ),
            onRetry = {},
            onAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun UsersListContentPreviewError() {
    MaterialTheme {
        UsersListContent(
            uiState =
                UsersUiState(
                    connectionState = ConnectionState.Disconnected,
                    communicationStatus = CommunicationStatusState.Error("Failed to reconnect"),
                    heartbeatCountdown = 0,
                    isLoading = false,
                    users = emptyList(),
                    errorMessage = "Connection error while loading users.",
                ),
            onRetry = {},
            onAction = {},
        )
    }
}
