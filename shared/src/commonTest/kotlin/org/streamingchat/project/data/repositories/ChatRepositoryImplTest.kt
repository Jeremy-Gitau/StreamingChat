package org.streamingchat.project.data.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.streamingchat.project.domain.model.*
import org.streamingchat.project.sources.network.ChatRemoteDataSource
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeChatRemoteDataSource(
    private val events: List<ChatEvent>,
) : ChatRemoteDataSource {
    var receivedHistory: List<ChatTurn>? = null

    override fun streamReply(
        model: AiModel,
        history: List<ChatTurn>,
    ): Flow<ChatEvent> =
        flow {
            receivedHistory = history
            events.forEach { emit(it) }
        }
}

class ChatRepositoryImplTest {
    private val scripted =
        listOf(
            ChatEvent.Thinking("hmm"),
            ChatEvent.Text("Hello"),
            ChatEvent.Finished(FinishReason.Stop),
        )

    @Test
    fun passesEventsThroughUnchanged() =
        runTest {
            val repo = ChatRepositoryImpl(FakeChatRemoteDataSource(scripted), InMemorySettingsRepository())

            val events = repo.streamReply(ModelCatalog.NemotronSuper, listOf(ChatTurn(Role.User, "hi"))).toList()

            assertEquals(scripted, events)
        }

    @Test
    fun prependsSystemPrompt() =
        runTest {
            val fake = FakeChatRemoteDataSource(scripted)
            val repo = ChatRepositoryImpl(fake, InMemorySettingsRepository(ChatSettings(systemPrompt = "Be brief.")))

            repo.streamReply(ModelCatalog.NemotronSuper, listOf(ChatTurn(Role.User, "hi"))).toList()

            assertEquals(
                listOf(ChatTurn(Role.System, "Be brief."), ChatTurn(Role.User, "hi")),
                fake.receivedHistory,
            )
        }

    @Test
    fun doesNotDuplicateExistingSystemPrompt() =
        runTest {
            val fake = FakeChatRemoteDataSource(scripted)
            val repo = ChatRepositoryImpl(fake, InMemorySettingsRepository(ChatSettings(systemPrompt = "Be brief.")))
            val history = listOf(ChatTurn(Role.System, "Custom"), ChatTurn(Role.User, "hi"))

            repo.streamReply(ModelCatalog.NemotronSuper, history).toList()

            assertEquals(history, fake.receivedHistory)
        }
}
