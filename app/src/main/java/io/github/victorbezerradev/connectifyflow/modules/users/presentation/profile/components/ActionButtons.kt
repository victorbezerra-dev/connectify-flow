package io.github.victorbezerradev.connectifyflow.modules.users.presentation.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.victorbezerradev.connectifyflow.core.ui.themes.BrandGreen
import io.github.victorbezerradev.connectifyflow.core.ui.themes.BrandGreenLight
import io.github.victorbezerradev.connectifyflow.core.ui.themes.BrandLilac

@Composable
fun EmailActionButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 14.dp),
        shape = RoundedCornerShape(16.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = BrandLilac,
                contentColor = Color.White,
            ),
    ) {
        Icon(
            imageVector = Icons.Default.Email,
            contentDescription = null,
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = "Send e-mail",
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun SecondaryActionsRow(
    hasPhone: Boolean,
    hasProfileLink: Boolean,
    onCallClick: () -> Unit,
    onOpenWebsiteClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (hasPhone) {
            ActionRowButton(
                onClick = onCallClick,
                icon = Icons.Default.Phone,
                label = "Call",
                modifier = Modifier.weight(1f),
            )
        }

        if (hasProfileLink) {
            ActionRowButton(
                onClick = onOpenWebsiteClick,
                icon = Icons.Default.Language,
                label = "Web",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
fun ActionRowButton(
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 14.dp),
        shape = RoundedCornerShape(16.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = BrandGreenLight,
                contentColor = BrandGreen,
            ),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
        )
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
        )
    }
}
