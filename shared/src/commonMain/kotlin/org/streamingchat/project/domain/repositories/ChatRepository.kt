package org.streamingchat.project.domain.repositories

import kotlinx.coroutines.flow.Flow
import org.streamingchat.project.domain.model.AiModel
import org.streamingchat.project.domain.model.ChatEvent
import org.streamingchat.project.domain.model.ChatTurn

interface ChatRepository {
    fun availableModels(): List<AiModel>

    fun streamReply(
        model: AiModel,
        history: List<ChatTurn>,
    ): Flow<ChatEvent>
}
