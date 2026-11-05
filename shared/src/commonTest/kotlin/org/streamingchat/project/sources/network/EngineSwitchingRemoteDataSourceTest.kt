package org.streamingchat.project.sources.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.streamingchat.project.data.repositories.InMemorySettingsRepository
import org.streamingchat.project.domain.model.AiModel
import org.streamingchat.project.domain.model.ChatEvent
import org.streamingchat.project.domain.model.ChatTurn
import org.streamingchat.project.domain.model.ModelCatalog
import org.streamingchat.project.domain.model.StreamEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

private class NamedRemote(
    private val name: String,
) : ChatRemoteDataSource {
    override fun streamReply(
        model: AiModel,
        history: List<ChatTurn>,
    ): Flow<ChatEvent> = flowOf(ChatEvent.Text(name))
}

class EngineSwitchingRemoteDataSourceTest {
    private val settings = InMemorySettingsRepository()
    private var koogCreated = false
    private val source =
        EngineSwitchingRemoteDataSource(
            settings = settings,
            sse = NamedRemote("sse"),
            koog = lazy { koogCreated = true; NamedRemote("koog") },
        )

    private suspend fun reply() = source.streamReply(ModelCatalog.NemotronSuper, emptyList()).toList()

    @Test
    fun switchingTheSettingChangesTheEngine() =
        runTest {
            assertEquals(listOf<ChatEvent>(ChatEvent.Text("sse")), reply())

            settings.update { copy(engine = StreamEngine.Koog) }

            assertEquals(listOf<ChatEvent>(ChatEvent.Text("koog")), reply())
        }

    @Test
    fun engineIsChosenWhenCollectedNotWhenCalled() =
        runTest {
            val pending = source.streamReply(ModelCatalog.NemotronSuper, emptyList())
            settings.update { copy(engine = StreamEngine.Koog) }

            assertEquals(listOf<ChatEvent>(ChatEvent.Text("koog")), pending.toList())
        }

    @Test
    fun koogIsNotCreatedUntilPicked() =
        runTest {
            reply()
            assertFalse(koogCreated)
        }
}
