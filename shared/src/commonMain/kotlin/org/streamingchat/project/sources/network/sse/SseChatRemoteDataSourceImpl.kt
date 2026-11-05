package org.streamingchat.project.sources.network.sse

import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.takeWhile
import org.streamingchat.project.domain.model.AiModel
import org.streamingchat.project.domain.model.ChatEvent
import org.streamingchat.project.domain.model.ChatTurn
import org.streamingchat.project.sources.network.ChatRemoteDataSource
import org.streamingchat.project.sources.network.clients.NetworkConfig
import org.streamingchat.project.sources.network.clients.streamLines
import org.streamingchat.project.sources.network.mapper.toChatEvents
import org.streamingchat.project.sources.network.mapper.toChatRequestBody

class SseChatRemoteDataSourceImpl(
    private val client: HttpClient,
    private val config: NetworkConfig,
) : ChatRemoteDataSource {
    override fun streamReply(
        model: AiModel,
        history: List<ChatTurn>,
    ): Flow<ChatEvent> =
        client
            .streamLines(
                url = "${config.ollamaBaseUrl}/v1/chat/completions",
                body = history.toChatRequestBody(model.id),
            ).parseSse()
            .takeWhile { it.data != "[DONE]" }
            .toChatEvents()
            .handleLeakedThinkTags()
}
