package io.github.victorbezerradev.connectifyflow.modules.users.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String,
    val phone: String?,
    val status: String,
    val profileImageUrl: String?,
    val profileLinkUrl: String?,
)
