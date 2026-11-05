package org.streamingchat.project.sources.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import org.streamingchat.project.domain.model.AiModel
import org.streamingchat.project.domain.model.ChatEvent
import org.streamingchat.project.domain.model.ChatTurn
import org.streamingchat.project.domain.model.StreamEngine
import org.streamingchat.project.domain.repositories.SettingsRepository

class EngineSwitchingRemoteDataSource(
    private val settings: SettingsRepository,
    private val sse: ChatRemoteDataSource,
    private val koog: Lazy<ChatRemoteDataSource>,
) : ChatRemoteDataSource {
    override fun streamReply(
        model: AiModel,
        history: List<ChatTurn>,
    ): Flow<ChatEvent> =
        flow {
            val engine =
                when (settings.settings.value.engine) {
                    StreamEngine.Sse -> sse
                    StreamEngine.Koog -> koog.value
                }
            emitAll(engine.streamReply(model, history))
        }
}
