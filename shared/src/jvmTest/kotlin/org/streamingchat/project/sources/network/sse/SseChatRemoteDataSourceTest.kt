package org.streamingchat.project.sources.network.sse

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.streamingchat.project.domain.model.*
import org.streamingchat.project.sources.network.clients.HttpClientFactory
import org.streamingchat.project.sources.network.clients.NetworkConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SseChatRemoteDataSourceTest {
    @Test
    fun streamsThinkingTextAndFinish() =
        runBlocking {
            val config = NetworkConfig()
            val client = HttpClientFactory.create(config)
            val source = SseChatRemoteDataSourceImpl(client, config)

            val events =
                source
                    .streamReply(
                        model = ModelCatalog.NemotronSuper,
                        history = listOf(ChatTurn(Role.User, "Explain Kotlin Flow in 2 sentences.")),
                    ).toList()

            events.forEach(::println)

            assertTrue(events.any { it is ChatEvent.Thinking }, "expected thinking events")
            assertTrue(events.any { it is ChatEvent.Text }, "expected text events")
            assertEquals(ChatEvent.Finished(FinishReason.Stop), events.last())
            client.close()
        }
}
