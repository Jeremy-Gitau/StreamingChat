package org.streamingchat.project.sources.network.koog

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.streamingchat.project.domain.model.AiProvider
import org.streamingchat.project.domain.model.ChatEvent
import org.streamingchat.project.domain.model.ChatTurn
import org.streamingchat.project.domain.model.ModelCatalog
import org.streamingchat.project.domain.model.Role
import org.streamingchat.project.sources.network.clients.KoogClientFactory
import org.streamingchat.project.sources.network.clients.NetworkConfig
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

class KoogChatRemoteDataSourceTest {
    @Test
    fun streamsThinkingTextAndFinish(): Unit =
        runBlocking {
            val config = NetworkConfig()
            val ollama = KoogClientFactory.ollama(config, KoogClientFactory.httpFactory())
            val source = KoogChatRemoteDataSourceImpl(mapOf(AiProvider.Ollama to ollama))

            val events =
                source
                    .streamReply(
                        model = ModelCatalog.NemotronSuper,
                        history = listOf(ChatTurn(Role.User, "Explain Kotlin Flow in 2 sentences.")),
                    ).toList()

            events.forEach(::println)

            assertTrue(events.any { it is ChatEvent.Thinking }, "expected thinking events")
            assertTrue(events.any { it is ChatEvent.Text }, "expected text events")
            assertIs<ChatEvent.Finished>(events.last())
        }
}
