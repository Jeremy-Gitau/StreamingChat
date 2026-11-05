package org.streamingchat.project.sources.network.sse

import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.streamingchat.project.domain.model.ChatTurn
import org.streamingchat.project.domain.model.Role
import org.streamingchat.project.sources.network.clients.HttpClientFactory
import org.streamingchat.project.sources.network.clients.NetworkConfig
import org.streamingchat.project.sources.network.clients.streamLines
import org.streamingchat.project.sources.network.mapper.toChatRequestBody
import kotlin.test.Test
import kotlin.test.assertTrue

class StreamLinesTest {
    @Test
    fun streamsRawSseLines() =
        runBlocking {
            val config = NetworkConfig()
            val client = HttpClientFactory.create(config)
            val body =
                """{"model":"nemotron-3-super:cloud","stream":true,""" +
                    """"messages":[{"role":"user","content":"Say hi in three words."}]}"""

            val lines = client.streamLines("${config.ollamaBaseUrl}/v1/chat/completions", body).toList()

            lines.forEach(::println)
            assertTrue(lines.contains("data: [DONE]"), "expected the [DONE] sentinel")
            client.close()
        }

    @Test
    fun printsRawLinesForSecondTurn() =
        runBlocking {
            val config = NetworkConfig()
            val client = HttpClientFactory.create(config)
            val body =
                listOf(
                    ChatTurn(Role.User, "hi"),
                    ChatTurn(Role.Assistant, "Hi! How can I assist you today?"),
                    ChatTurn(Role.User, "i am fine how are you"),
                ).toChatRequestBody("nemotron-3-super:cloud")

            client
                .streamLines("${config.ollamaBaseUrl}/v1/chat/completions", body)
                .filter { "</think>" in it || "<think>" in it }
                .collect { println("TAG FOUND: $it") }

            client.close()
        }
}
