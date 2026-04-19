package io.github.victorbezerradev.connectifyflow.modules.users.presentation.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import io.github.victorbezerradev.connectifyflow.core.ui.themes.BrandLilac
import io.github.victorbezerradev.connectifyflow.core.ui.themes.BrandLilacLight

@Composable
fun ProfilePlaceholder(name: String) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(BrandLilacLight),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name.firstOrNull()?.uppercase() ?: "?",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = BrandLilac,
        )
    }
}
