package org.streamingchat.project.sources.network.clients

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp

actual val defaultOllamaBaseUrl: String = "http://10.0.2.2:11434"

actual fun platformHttpClient(configure: HttpClientConfig<*>.() -> Unit): HttpClient = HttpClient(OkHttp) { configure(this) }
