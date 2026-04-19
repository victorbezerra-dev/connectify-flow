package io.github.victorbezerradev.connectifyflow.modules.users.infra.dtos

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    @SerialName("id")
    val id: String? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("email")
    val email: String? = null,
    @SerialName("phone")
    val phone: String? = null,
    @SerialName("status")
    val status: String? = null,
    @SerialName("profileImage")
    val profileImage: String? = null,
    @SerialName("profileLink")
    val profileLink: String? = null,
)
