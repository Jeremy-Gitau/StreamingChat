package org.streamingchat.project.sources.network.koog

import ai.koog.http.client.KoogHttpClientException
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.LLMClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import org.streamingchat.project.domain.model.*
import org.streamingchat.project.sources.network.ChatRemoteDataSource
import org.streamingchat.project.sources.network.HttpStreamException
import org.streamingchat.project.sources.network.mapper.toChatEventOrNull
import org.streamingchat.project.sources.network.mapper.toKoogModel

class KoogChatRemoteDataSourceImpl(
    private val clients: Map<AiProvider, LLMClient>,
) : ChatRemoteDataSource {
    override fun streamReply(
        model: AiModel,
        history: List<ChatTurn>,
    ): Flow<ChatEvent> =
        flow {
            val client =
                clients[model.provider]
                    ?: throw IllegalStateException("No client configured for ${model.provider}")

            val chatPrompt =
                prompt("chat") {
                    history.forEach { turn ->
                        when (turn.role) {
                            Role.System -> system(turn.text)
                            Role.User -> user(turn.text)
                            Role.Assistant -> assistant(turn.text)
                        }
                    }
                }

            var sawFinish = false

            emitAll(
                client
                    .executeStreaming(prompt = chatPrompt, model = model.toKoogModel())
                    .catch { throw it.asHttpStreamException() }
                    .mapNotNull { it.toChatEventOrNull() }
                    .onEach { if (it is ChatEvent.Finished) sawFinish = true },
            )

            // Koog's Ollama client ends without an End frame (as seen in the spike test).
            if (!sawFinish) emit(ChatEvent.Finished(FinishReason.Unknown))
        }
}

// Koog reports HTTP errors with its own type. Translate them so a 401 is never retried.
private fun Throwable.asHttpStreamException(): Throwable {
    val http = generateSequence(this) { it.cause }.filterIsInstance<KoogHttpClientException>().firstOrNull()
    val status = http?.statusCode ?: return this
    return HttpStreamException(status, http.errorBody.orEmpty())
}
