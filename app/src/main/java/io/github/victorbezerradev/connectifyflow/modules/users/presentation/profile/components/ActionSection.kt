package io.github.victorbezerradev.connectifyflow.modules.users.presentation.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.victorbezerradev.connectifyflow.core.ui.themes.BrandLilac

@Composable
fun ActionSection(
    hasPhone: Boolean,
    hasProfileLink: Boolean,
    onSendEmailClick: () -> Unit,
    onCallClick: () -> Unit,
    onOpenWebsiteClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BrandLilac,
            )

            EmailActionButton(onClick = onSendEmailClick)

            if (hasPhone || hasProfileLink) {
                SecondaryActionsRow(
                    hasPhone = hasPhone,
                    hasProfileLink = hasProfileLink,
                    onCallClick = onCallClick,
                    onOpenWebsiteClick = onOpenWebsiteClick,
                )
            }
        }
    }
}
