package org.streamingchat.project.ui.chat.models

import androidx.compose.runtime.Immutable
import org.streamingchat.project.domain.model.ChatTurn
import org.streamingchat.project.domain.model.Role

// @Immutable lets Compose skip finished messages: same values, no recomposition.
@Immutable
data class UiMessage(
    val id: Long,
    val role: Role,
    val text: String,
    val thinking: String = "",
    val thinkingMs: Long? = null,
    val status: MessageStatus = MessageStatus.Done,
    val retryLabel: String? = null,
)

internal fun List<UiMessage>.toHistory(): List<ChatTurn> = filter { it.text.isNotBlank() }.map { ChatTurn(it.role, it.text) }
