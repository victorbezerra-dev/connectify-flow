package io.github.victorbezerradev.connectifyflow.modules.users.infra.apis

import io.github.victorbezerradev.connectifyflow.modules.users.infra.dtos.UsersResponseDto
import retrofit2.http.GET

interface UsersApi {
    @GET("newloran2/testApp2026/main/service.json")
    suspend fun getUsers(): UsersResponseDto
}
