package io.github.victorbezerradev.connectifyflow.modules.users.infra

import io.github.victorbezerradev.connectifyflow.modules.users.domain.models.User
import io.github.victorbezerradev.connectifyflow.modules.users.domain.repositories.UsersRepository
import io.github.victorbezerradev.connectifyflow.modules.users.infra.apis.UsersApi
import io.github.victorbezerradev.connectifyflow.modules.users.infra.mappers.toDomain
import javax.inject.Inject

class UsersRepositoryImpl
    @Inject
    constructor(
        private val api: UsersApi,
    ) : UsersRepository {
        override suspend fun getUsers(): List<User> {
            return api
                .getUsers()
                .customers
                .map { it.toDomain() }
        }
    }
