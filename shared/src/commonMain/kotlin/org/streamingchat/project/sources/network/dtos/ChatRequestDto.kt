package org.streamingchat.project.sources.network.dtos

import kotlinx.serialization.Serializable

@Serializable
internal data class ChatRequestDto(
    val model: String,
    val messages: List<MessageDto>,
    val stream: Boolean = true,
)

@Serializable
internal data class MessageDto(
    val role: String,
    val content: String,
)
