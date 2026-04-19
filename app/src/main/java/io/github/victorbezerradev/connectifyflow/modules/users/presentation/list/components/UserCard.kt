package io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import io.github.victorbezerradev.connectifyflow.modules.users.domain.models.User

@Composable
fun UserCard(
    user: User,
    onProfileClick: () -> Unit,
    onWebPageClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isImageModalVisible by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            UserAvatar(
                user = user,
                onImageClick = { isImageModalVisible = true },
            )

            Column(
                modifier = Modifier.weight(1f),
            ) {
                UserHeader(user = user)

                Spacer(modifier = Modifier.height(8.dp))

                UserMetaRow(user = user)

                Spacer(modifier = Modifier.height(8.dp))

                UserActionsRow(
                    user = user,
                    onProfileClick = onProfileClick,
                    onWebPageClick = onWebPageClick,
                )
            }
        }
    }

    if (isImageModalVisible) {
        FullScreenImageModal(
            imageUrl = user.profileImageUrl,
            userName = user.name,
            onDismiss = { isImageModalVisible = false },
        )
    }
}

@Composable
private fun UserAvatar(
    user: User,
    onImageClick: () -> Unit,
) {
    SubcomposeAsyncImage(
        model = user.profileImageUrl,
        contentDescription = "Photo of ${user.name}",
        modifier =
            Modifier
                .size(64.dp)
                .clip(CircleShape)
                .clickable { onImageClick() },
        contentScale = ContentScale.Crop,
        loading = {
            UserPlaceholder(name = user.name)
        },
        error = {
            UserPlaceholder(name = user.name)
        },
    )
}

@Composable
private fun UserHeader(user: User) {
    Column {
        Text(
            text = user.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = user.email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun UserMetaRow(user: User) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatusChip(status = user.status)

        user.phone?.let { phone ->
            UserPhone(phone = phone)
        }
    }
}

@Composable
private fun UserPhone(phone: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Outlined.Phone,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
        )

        Text(
            text = phone,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 4.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun UserActionsRow(
    user: User,
    onProfileClick: () -> Unit,
    onWebPageClick: (String) -> Unit,
) {
    val url = user.profileLinkUrl

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UserActionChip(
            label = "Profile",
            icon = Icons.Outlined.AccountCircle,
            color = MaterialTheme.colorScheme.primary,
            onClick = onProfileClick,
        )

        url?.let {
            UserActionChip(
                label = "Web Page",
                icon = Icons.Outlined.Language,
                color = MaterialTheme.colorScheme.secondary,
                onClick = { onWebPageClick(it) },
            )
        }
    }
}

@Composable
private fun UserActionChip(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
) {
    AssistChip(
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
        },
        colors =
            AssistChipDefaults.assistChipColors(
                labelColor = color,
                leadingIconContentColor = color,
            ),
        border =
            BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
            ),
    )
}

@Composable
private fun UserPlaceholder(name: String) {
    Box(
        modifier =
            Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name.firstOrNull()?.uppercase() ?: "?",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun StatusChip(status: String) {
    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = status.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}
