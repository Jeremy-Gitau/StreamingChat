package org.streamingchat.project.domain.model

enum class Role { System, User, Assistant }

data class ChatTurn(
    val role: Role,
    val text: String,
)

sealed interface ChatEvent {
    data class Thinking(
        val text: String,
    ) : ChatEvent

    data class Text(
        val text: String,
    ) : ChatEvent

    data class Finished(
        val reafson: FinishReason,
    ) : ChatEvent

    data object ReclassifyTextAsThinking : ChatEvent

    data class Retrying(
        val attempt: Int,
        val maxRetries: Int,
    ) : ChatEvent
}

enum class FinishReason { Stop, Length, ToolCalls, ContentFilter, Unknown }
