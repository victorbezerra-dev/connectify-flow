package io.github.victorbezerradev.connectifyflow.modules.users.presentation.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.victorbezerradev.connectifyflow.core.ui.themes.BrandGreen
import io.github.victorbezerradev.connectifyflow.core.ui.themes.BrandGreenLight

@Composable
fun StatusChip(
    status: String,
    modifier: Modifier = Modifier,
) {
    val isActive = status.lowercase() == "active"
    val containerColor = if (isActive) BrandGreenLight else Color(0xFFF5F5F5)
    val contentColor = if (isActive) BrandGreen else Color(0xFF757575)

    Surface(
        modifier = modifier,
        color = containerColor,
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(10.dp)
                        .background(contentColor, CircleShape),
            )

            Text(
                text = status.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor,
            )
        }
    }
}
