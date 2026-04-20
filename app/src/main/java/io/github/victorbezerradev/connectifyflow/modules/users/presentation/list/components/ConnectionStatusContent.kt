package io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.victorbezerradev.connectifyflow.core.websocket.ConnectionState
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.states.CommunicationStatusState

@Composable
fun ConnectionStatusContent(
    status: ConnectionState,
    modifier: Modifier = Modifier,
    communicationStatus: CommunicationStatusState = CommunicationStatusState.Idle,
    onRetry: () -> Unit = {},
) {
    val statusUi = status.toConnectionStatusUi()
    val communicationText = communicationStatus.toCommunicationText()
    val isConnecting = status is ConnectionState.Connecting
    val showRetryButton = status is ConnectionState.Error || status is ConnectionState.Disconnected || isConnecting

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            ConnectionStatusHeader()
            Spacer(modifier = Modifier.height(12.dp))
            ConnectionStatusRow(
                statusUi = statusUi,
                communicationText = communicationText,
            )

            if (showRetryButton) {
                Spacer(modifier = Modifier.height(16.dp))
                RetryButton(
                    onRetry = onRetry,
                    isLoading = isConnecting,
                )
            }
        }
    }
}

@Composable
private fun RetryButton(
    onRetry: () -> Unit,
    isLoading: Boolean,
) {
    Button(
        onClick = onRetry,
        enabled = !isLoading,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            }
            Text(
                text = if (isLoading) "Connecting..." else "Retry connection",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun ConnectionStatusHeader() {
    Text(
        text = "Connection status",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Medium,
    )
}

@Composable
private fun ConnectionStatusRow(
    statusUi: ConnectionStatusUi,
    communicationText: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ConnectionStatusIcon(statusColor = statusUi.color)
        ConnectionStatusTexts(
            statusText = statusUi.text,
            statusColor = statusUi.color,
            communicationText = communicationText,
        )
    }
}

@Composable
private fun ConnectionStatusIcon(statusColor: Color) {
    Surface(
        modifier = Modifier.size(40.dp),
        shape = CircleShape,
        color = statusColor.copy(alpha = 0.12f),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Wifi,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}

@Composable
private fun ConnectionStatusTexts(
    statusText: String,
    statusColor: Color,
    communicationText: String,
) {
    Column {
        Text(
            text = statusText,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = statusColor,
        )

        Text(
            text = communicationText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private data class ConnectionStatusUi(
    val text: String,
    val color: Color,
)

private fun ConnectionState.toConnectionStatusUi(): ConnectionStatusUi =
    when (this) {
        is ConnectionState.Connected ->
            ConnectionStatusUi(
                text = "Connected",
                color = Color(0xFF16A34A),
            )

        is ConnectionState.Connecting ->
            ConnectionStatusUi(
                text = "Connecting...",
                color = Color(0xFFF59E0B),
            )

        is ConnectionState.Disconnected ->
            ConnectionStatusUi(
                text = "Disconnected",
                color = Color(0xFFDC2626),
            )

        is ConnectionState.Error ->
            ConnectionStatusUi(
                text = "Connection Error",
                color = Color(0xFFDC2626),
            )
    }

private fun CommunicationStatusState.toCommunicationText(): String =
    when (this) {
        is CommunicationStatusState.Idle -> "No communication yet"
        is CommunicationStatusState.Sending -> "Sent: $message"
        is CommunicationStatusState.AwaitingResponse -> "Awaiting response..."
        is CommunicationStatusState.Received -> "Received: $message"
        is CommunicationStatusState.Error -> reason
    }
