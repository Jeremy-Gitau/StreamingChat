package org.streamingchat.project.sources.network.mapper

import kotlinx.serialization.json.Json
import org.streamingchat.project.domain.model.ChatTurn
import org.streamingchat.project.domain.model.Role
import org.streamingchat.project.sources.network.dtos.ChatRequestDto
import org.streamingchat.project.sources.network.dtos.MessageDto

private val requestJson = Json { encodeDefaults = true }

fun List<ChatTurn>.toChatRequestBody(modelId: String): String =
    requestJson.encodeToString(
        ChatRequestDto(
            model = modelId,
            messages = map { MessageDto(role = it.role.toWireRole(), content = it.text) },
        ),
    )

private fun Role.toWireRole(): String =
    when (this) {
        Role.System -> "system"
        Role.User -> "user"
        Role.Assistant -> "assistant"
    }
