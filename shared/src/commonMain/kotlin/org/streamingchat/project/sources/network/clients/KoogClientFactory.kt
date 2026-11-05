package org.streamingchat.project.sources.network.clients

import ai.koog.http.client.KoogHttpClient
import ai.koog.http.client.ktor.KtorKoogHttpClient
import ai.koog.prompt.executor.clients.ConnectionTimeoutConfig
import ai.koog.prompt.executor.ollama.client.OllamaClient
import io.ktor.client.plugins.HttpTimeoutConfig

object KoogClientFactory {
    fun httpFactory(): KoogHttpClient.Factory = KtorKoogHttpClient.Factory()

    fun ollama(
        config: NetworkConfig,
        httpFactory: KoogHttpClient.Factory,
    ): OllamaClient =
        OllamaClient(
            httpClientFactory = httpFactory,
            baseUrl = config.ollamaBaseUrl,
            timeoutConfig =
                ConnectionTimeoutConfig(
                    connectTimeoutMillis = config.connectTimeoutMs,
                    socketTimeoutMillis = config.idleTimeoutMs,
                    requestTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS,
                ),
        )
}
