package io.github.victorbezerradev.connectifyflow.modules.users.infra.dtos

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UsersResponseDto(
    @SerialName("customers")
    val customers: List<UserDto> = emptyList(),
)
