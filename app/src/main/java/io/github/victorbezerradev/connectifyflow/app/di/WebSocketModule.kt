package io.github.victorbezerradev.connectifyflow.app.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.victorbezerradev.connectifyflow.core.websocket.OkHttpWebSocketClient
import io.github.victorbezerradev.connectifyflow.core.websocket.WebSocketClient
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

private const val WEB_SOCKET_URL = "wss://ws.postman-echo.com/raw"

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WebSocketUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WebSocketOkHttpClient

@Module
@InstallIn(SingletonComponent::class)
abstract class WebSocketModule {
    @Binds
    @Singleton
    abstract fun bindWebSocketClient(impl: OkHttpWebSocketClient): WebSocketClient

    companion object {
        @Provides
        @Singleton
        @WebSocketOkHttpClient
        fun provideWebSocketOkHttpClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .pingInterval(60, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.SECONDS)
                .writeTimeout(35, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .build()
        }

        @Provides
        @WebSocketUrl
        fun provideWebSocketUrl(): String = WEB_SOCKET_URL
    }
}
