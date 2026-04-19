package io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.victorbezerradev.connectifyflow.core.websocket.ConnectionState
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.states.CommunicationStatusState

@Composable
fun ExpandableConnectionCard(
    modifier: Modifier = Modifier,
    title: String = "Animation",
    status: ConnectionState = ConnectionState.Connected,
    communicationStatus: CommunicationStatusState = CommunicationStatusState.Idle,
    heartbeatCountdown: Int = 30,
) {
    var expanded by remember { mutableStateOf(false) }
    val arrowRotation by rememberArrowRotation(expanded = expanded)

    val headerUiState =
        ExpandableConnectionHeaderUiState(
            title = title,
            status = status,
            expanded = expanded,
            arrowRotation = arrowRotation,
            heartbeatCountdown = heartbeatCountdown,
        )

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .animateContentSize(),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            ExpandableConnectionHeader(
                uiState = headerUiState,
                onToggleExpanded = { expanded = !expanded },
            )

            ExpandableConnectionContent(
                expanded = expanded,
                status = status,
                communicationStatus = communicationStatus,
            )
        }
    }
}

private data class ExpandableConnectionHeaderUiState(
    val title: String,
    val status: ConnectionState,
    val expanded: Boolean,
    val arrowRotation: Float,
    val heartbeatCountdown: Int,
)

@Composable
private fun ExpandableConnectionHeader(
    uiState: ExpandableConnectionHeaderUiState,
    onToggleExpanded: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        HeaderTitle(
            title = uiState.title,
            status = uiState.status,
        )

        HeaderActions(
            expanded = uiState.expanded,
            arrowRotation = uiState.arrowRotation,
            heartbeatCountdown = uiState.heartbeatCountdown,
            onToggleExpanded = onToggleExpanded,
        )
    }
}

@Composable
private fun HeaderTitle(
    title: String,
    status: ConnectionState,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatusIndicator(status = status)
        Spacer(modifier = Modifier.size(12.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun HeaderActions(
    expanded: Boolean,
    arrowRotation: Float,
    heartbeatCountdown: Int,
    onToggleExpanded: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HeartbeatTimer(countdown = heartbeatCountdown)
        Spacer(modifier = Modifier.size(8.dp))

        ExpandCollapseButton(
            expanded = expanded,
            arrowRotation = arrowRotation,
            onClick = onToggleExpanded,
        )
    }
}

@Composable
private fun ExpandCollapseButton(
    expanded: Boolean,
    arrowRotation: Float,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
    ) {
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = if (expanded) "Collapse" else "Expand",
            modifier = Modifier.rotate(arrowRotation),
        )
    }
}

@Composable
private fun ExpandableConnectionContent(
    expanded: Boolean,
    status: ConnectionState,
    communicationStatus: CommunicationStatusState,
) {
    AnimatedVisibility(visible = expanded) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ConnectionStatusAnimation(
                connectionState = status,
                communicationStatus = communicationStatus,
                modifier = Modifier.padding(bottom = 20.dp),
            )

            ConnectionStatusContent(
                status = status,
                communicationStatus = communicationStatus,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun rememberArrowRotation(expanded: Boolean): State<Float> =
    animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "arrowRotation",
    )
