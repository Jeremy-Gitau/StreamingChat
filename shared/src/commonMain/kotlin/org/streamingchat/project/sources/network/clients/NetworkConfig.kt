package org.streamingchat.project.sources.network.clients

import org.streamingchat.project.domain.model.StreamEngine

data class NetworkConfig(
    val ollamaBaseUrl: String = defaultOllamaBaseUrl,
    val connectTimeoutMs: Long = 10_000,
    val idleTimeoutMs: Long = 60_000,
    val enableLogging: Boolean = true,
    val engine: StreamEngine = StreamEngine.Sse, // only the starting value; switchable at runtime in settings
)

expect val defaultOllamaBaseUrl: String
