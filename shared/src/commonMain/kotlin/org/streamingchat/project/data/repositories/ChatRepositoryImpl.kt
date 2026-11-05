package org.streamingchat.project.data.repositories

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retryWhen
import kotlinx.serialization.SerializationException
import org.streamingchat.project.domain.model.*
import org.streamingchat.project.domain.repositories.ChatRepository
import org.streamingchat.project.domain.repositories.SettingsRepository
import org.streamingchat.project.sources.network.ChatRemoteDataSource
import org.streamingchat.project.sources.network.HttpStreamException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds

class ChatRepositoryImpl(
    private val remote: ChatRemoteDataSource,
    private val settings: SettingsRepository,
    private val basePolicy: RetryPolicy = RetryPolicy(),
) : ChatRepository {
    override fun availableModels(): List<AiModel> = ModelCatalog.all

    override fun streamReply(
        model: AiModel,
        history: List<ChatTurn>,
    ): Flow<ChatEvent> =
        flow {
            val current = settings.settings.value
            val policy = basePolicy.copy(maxRetries = current.maxRetries)
            var reachedUser = false

            emitAll(
                remote
                    .streamReply(model, history.withSystemPrompt(current.systemPrompt))
                    .onEach { reachedUser = true }
                    .retryWhen { cause, attempt ->
                        val retry = !reachedUser && attempt < policy.maxRetries && cause.isRetryable()
                        if (retry) {
                            emit(ChatEvent.Retrying(attempt.toInt() + 1, policy.maxRetries))
                            delay(policy.delayFor(attempt).milliseconds)
                        }
                        retry
                    },
            )
        }
}

private fun List<ChatTurn>.withSystemPrompt(systemPrompt: String?): List<ChatTurn> =
    if (systemPrompt.isNullOrBlank() || firstOrNull()?.role == Role.System) {
        this
    } else {
        listOf(ChatTurn(Role.System, systemPrompt)) + this
    }

private fun Throwable.isRetryable(): Boolean =
    when (this) {
        is CancellationException -> false
        is HttpStreamException -> status == 408 || status == 429 || status in 500..599
        is SerializationException -> false
        else -> true
    }
