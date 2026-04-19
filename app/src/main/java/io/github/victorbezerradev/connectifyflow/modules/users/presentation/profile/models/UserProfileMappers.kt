package io.github.victorbezerradev.connectifyflow.modules.users.presentation.profile.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Tag
import io.github.victorbezerradev.connectifyflow.modules.users.domain.models.User
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.profile.states.ProfileActionsState

fun User.toProfileActionsState(): ProfileActionsState {
    return ProfileActionsState(
        phone = phone?.takeIf { it.isNotBlank() },
        profileLink = profileLinkUrl?.takeIf { it.isNotBlank() },
    )
}

fun User.toInfoItems(): List<InfoItemData> {
    val phoneText = phone?.takeIf { it.isNotBlank() } ?: "Not informed"
    val profileLinkText = profileLinkUrl?.takeIf { it.isNotBlank() } ?: "Not informed"

    return listOf(
        InfoItemData(
            label = "User ID",
            value = id,
            icon = Icons.Default.Tag,
        ),
        InfoItemData(
            label = "Full Name",
            value = name,
            icon = Icons.Default.Person,
        ),
        InfoItemData(
            label = "Email Address",
            value = email,
            icon = Icons.Default.Email,
        ),
        InfoItemData(
            label = "Phone Number",
            value = phoneText,
            icon = Icons.Default.Phone,
        ),
        InfoItemData(
            label = "Personal Website",
            value = profileLinkText,
            icon = Icons.Default.Link,
        ),
    )
}
