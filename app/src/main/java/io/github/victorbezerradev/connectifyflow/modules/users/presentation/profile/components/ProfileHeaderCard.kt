package io.github.victorbezerradev.connectifyflow.modules.users.presentation.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.victorbezerradev.connectifyflow.core.ui.themes.BrandLilac
import io.github.victorbezerradev.connectifyflow.modules.users.domain.models.User

@Composable
fun ProfileHeaderCard(
    user: User,
    onImageClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            HeaderGradientBackground()

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 45.dp,
                            bottom = 24.dp,
                            start = 20.dp,
                            end = 20.dp,
                        ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ProfileImage(
                    imageUrl = user.profileImageUrl,
                    userName = user.name,
                    onClick = onImageClick,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = user.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )

                Spacer(modifier = Modifier.height(16.dp))

                StatusChip(status = user.status)
            }
        }
    }
}

@Composable
private fun HeaderGradientBackground() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(110.dp)
                .background(
                    brush =
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    BrandLilac.copy(alpha = 0.2f),
                                    Color.Transparent,
                                ),
                        ),
                    shape =
                        RoundedCornerShape(
                            bottomStart = 32.dp,
                            bottomEnd = 32.dp,
                        ),
                ),
    )
}
