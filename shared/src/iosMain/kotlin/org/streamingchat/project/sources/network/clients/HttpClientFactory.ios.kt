package org.streamingchat.project.sources.network.clients

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.darwin.Darwin

actual val defaultOllamaBaseUrl: String = "http://localhost:11434"

actual fun platformHttpClient(configure: HttpClientConfig<*>.() -> Unit): HttpClient = HttpClient(Darwin) { configure(this) }
