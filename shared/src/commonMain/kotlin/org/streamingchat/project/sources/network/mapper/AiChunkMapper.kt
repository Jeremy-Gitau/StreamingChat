package org.streamingchat.project.sources.network.mapper

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform
import kotlinx.serialization.json.Json
import org.streamingchat.project.domain.model.ChatEvent
import org.streamingchat.project.sources.network.dtos.ChunkDto
import org.streamingchat.project.sources.network.sse.SseEvent

private val json = Json { ignoreUnknownKeys = true }

fun Flow<SseEvent>.toChatEvents(): Flow<ChatEvent> =
    transform { sse ->
        val chunk = json.decodeFromString<ChunkDto>(sse.data)
        val choice = chunk.choices.firstOrNull() ?: return@transform

        val thinking = choice.delta.reasoning ?: choice.delta.reasoningContent
        thinking?.takeIf { it.isNotEmpty() }?.let { emit(ChatEvent.Thinking(it)) }
        choice.delta.content
            ?.takeIf { it.isNotEmpty() }
            ?.let { emit(ChatEvent.Text(it)) }
        choice.finishReason?.let { emit(ChatEvent.Finished(it.toFinishReason())) }
    }
