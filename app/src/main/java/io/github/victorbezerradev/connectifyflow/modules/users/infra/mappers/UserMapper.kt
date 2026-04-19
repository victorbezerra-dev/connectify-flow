package io.github.victorbezerradev.connectifyflow.modules.users.infra.mappers

import android.util.Patterns
import io.github.victorbezerradev.connectifyflow.modules.users.domain.models.User
import io.github.victorbezerradev.connectifyflow.modules.users.infra.dtos.UserDto

fun UserDto.toDomain(): User {
    val baseAvatarUrl = "https://raw.githubusercontent.com/newloran2/testApp2026/main/imagens"

    val profileImageUrl =
        profileImage
            ?.trim()
            ?.takeIf { it.isNotBlank() && it != "null" }
            ?.substringAfterLast("/")
            ?.takeIf { it.isNotBlank() }
            ?.let { fileName -> "$baseAvatarUrl/$fileName" }

    return User(
        id = id.orEmpty().ifBlank { "unknown-id" },
        name = name.orEmpty().ifBlank { "User without name" },
        email = email.orEmpty().ifBlank { "email-not-provided@unknown.com" },
        phone = phone?.takeIf { it.isNotBlank() },
        status = status.orEmpty().ifBlank { "unknown" },
        profileImageUrl = profileImageUrl,
        profileLinkUrl =
            profileLink
                ?.takeIf { it.isNotBlank() }
                ?.takeIf(::isValidHttpUrl),
    )
}

private fun isValidHttpUrl(value: String): Boolean {
    val isWebUrl = Patterns.WEB_URL.matcher(value).matches()
    val startsCorrectly = value.startsWith("http://") || value.startsWith("https://")
    return isWebUrl && startsCorrectly
}
