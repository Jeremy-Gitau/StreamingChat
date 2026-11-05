package org.streamingchat.project.data.repositories

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.streamingchat.project.domain.model.AiModel
import org.streamingchat.project.domain.model.ChatEvent
import org.streamingchat.project.domain.model.ChatSettings
import org.streamingchat.project.domain.model.ChatEvent.Finished
import org.streamingchat.project.domain.model.ChatEvent.Retrying
import org.streamingchat.project.domain.model.ChatEvent.Text
import org.streamingchat.project.domain.model.ChatTurn
import org.streamingchat.project.domain.model.FinishReason
import org.streamingchat.project.domain.model.ModelCatalog
import org.streamingchat.project.domain.model.Role
import org.streamingchat.project.sources.network.ChatRemoteDataSource
import org.streamingchat.project.sources.network.HttpStreamException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

// Fails the first [failures] attempts (optionally after emitting [beforeFailure]), then succeeds.
private class FlakyRemote(
    private val failures: Int,
    private val beforeFailure: List<ChatEvent> = emptyList(),
    private val error: () -> Throwable = { IllegalStateException("Connection refused") },
) : ChatRemoteDataSource {
    var attempts = 0

    override fun streamReply(
        model: AiModel,
        history: List<ChatTurn>,
    ): Flow<ChatEvent> =
        flow {
            attempts++
            if (attempts <= failures) {
                beforeFailure.forEach { emit(it) }
                throw error()
            }
            emit(Text("Hello"))
            emit(Finished(FinishReason.Stop))
        }
}

class ChatRepositoryRetryTest {
    private val history = listOf(ChatTurn(Role.User, "hi"))

    @OptIn(ExperimentalCoroutinesApi::class) // testScheduler.currentTime
    @Test
    fun retriesThenSucceeds() =
        runTest {
            val remote = FlakyRemote(failures = 2)
            val repo = ChatRepositoryImpl(remote, InMemorySettingsRepository())

            val events = repo.streamReply(ModelCatalog.NemotronSuper, history).toList()

            assertEquals(
                listOf(Retrying(1, 3), Retrying(2, 3), Text("Hello"), Finished(FinishReason.Stop)),
                events,
            )
            assertEquals(3, remote.attempts)
            assertEquals(3_000, testScheduler.currentTime) // 1s + 2s of virtual time
        }

    @Test
    fun givesUpAfterMaxRetries() =
        runTest {
            val remote = FlakyRemote(failures = Int.MAX_VALUE)
            val repo = ChatRepositoryImpl(remote, InMemorySettingsRepository())
            val events = mutableListOf<ChatEvent>()

            assertFailsWith<IllegalStateException> {
                repo.streamReply(ModelCatalog.NemotronSuper, history).toList(events)
            }

            assertEquals(listOf<ChatEvent>(Retrying(1, 3), Retrying(2, 3), Retrying(3, 3)), events)
            assertEquals(4, remote.attempts)
        }

    @Test
    fun neverRetriesAfterFirstToken() =
        runTest {
            val remote = FlakyRemote(failures = 1, beforeFailure = listOf(Text("Hel")))
            val repo = ChatRepositoryImpl(remote, InMemorySettingsRepository())
            val events = mutableListOf<ChatEvent>()

            assertFailsWith<IllegalStateException> {
                repo.streamReply(ModelCatalog.NemotronSuper, history).toList(events)
            }

            assertEquals(listOf<ChatEvent>(Text("Hel")), events)
            assertEquals(1, remote.attempts)
        }

    @Test
    fun doesNotRetryUnauthorized() =
        runTest {
            val remote = FlakyRemote(failures = 1, error = { HttpStreamException(401, "unauthorized") })
            val repo = ChatRepositoryImpl(remote, InMemorySettingsRepository())

            assertFailsWith<HttpStreamException> {
                repo.streamReply(ModelCatalog.NemotronSuper, history).toList()
            }

            assertEquals(1, remote.attempts)
        }

    @Test
    fun retriesServerErrors() =
        runTest {
            val remote = FlakyRemote(failures = 1, error = { HttpStreamException(503, "busy") })
            val repo = ChatRepositoryImpl(remote, InMemorySettingsRepository())

            val events = repo.streamReply(ModelCatalog.NemotronSuper, history).toList()

            assertEquals(Retrying(1, 3), events.first())
            assertEquals(2, remote.attempts)
        }

    @Test
    fun readsRetryCountFromSettingsWhenCollected() =
        runTest {
            val remote = FlakyRemote(failures = Int.MAX_VALUE)
            val settings = InMemorySettingsRepository()
            val repo = ChatRepositoryImpl(remote, settings)

            val reply = repo.streamReply(ModelCatalog.NemotronSuper, history) // created with 3...
            settings.update { copy(maxRetries = 1) } // ...changed before collection
            val events = mutableListOf<ChatEvent>()
            assertFailsWith<IllegalStateException> { reply.toList(events) }

            assertEquals(listOf<ChatEvent>(Retrying(1, 1)), events)
            assertEquals(2, remote.attempts)
        }
}
