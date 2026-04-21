package io.github.victorbezerradev.connectifyflow.modules.users.domain.interfaces

import io.github.victorbezerradev.connectifyflow.modules.users.domain.models.User

interface UsersRepository {
    suspend fun getUsers(): List<User>
}
