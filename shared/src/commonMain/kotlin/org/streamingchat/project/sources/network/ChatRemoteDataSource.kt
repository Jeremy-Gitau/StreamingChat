package org.streamingchat.project.sources.network

import kotlinx.coroutines.flow.Flow
import org.streamingchat.project.domain.model.AiModel
import org.streamingchat.project.domain.model.ChatEvent
import org.streamingchat.project.domain.model.ChatTurn

interface ChatRemoteDataSource {
    fun streamReply(
        model: AiModel,
        history: List<ChatTurn>,
    ): Flow<ChatEvent>
}
