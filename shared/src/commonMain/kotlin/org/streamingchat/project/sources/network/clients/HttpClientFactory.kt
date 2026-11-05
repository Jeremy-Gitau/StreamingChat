package org.streamingchat.project.sources.network.clients

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE

expect fun platformHttpClient(configure: HttpClientConfig<*>.() -> Unit): HttpClient

object HttpClientFactory {
    fun create(config: NetworkConfig): HttpClient =
        platformHttpClient {
            install(HttpTimeout) {
                connectTimeoutMillis = config.connectTimeoutMs
                socketTimeoutMillis = config.idleTimeoutMs
                requestTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
            }
            if (config.enableLogging) {
                install(Logging) {
                    logger = Logger.SIMPLE
                    level = LogLevel.HEADERS
                }
            }
            expectSuccess = false
        }
}
