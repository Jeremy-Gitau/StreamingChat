package org.streamingchat.project.sources.network

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.runBlocking
import org.streamingchat.project.sources.network.clients.HttpClientFactory
import org.streamingchat.project.sources.network.clients.NetworkConfig
import kotlin.test.Test
import kotlin.test.assertEquals

class HttpClientFactoryTest {
    @Test
    fun ollamaIsReachable() =
        runBlocking {
            val config = NetworkConfig()
            val client = HttpClientFactory.create(config)

            val response = client.get("${config.ollamaBaseUrl}/api/version")
            println("status=${response.status} body=${response.bodyAsText()}")

            assertEquals(200, response.status.value)
            client.close()
        }
}
