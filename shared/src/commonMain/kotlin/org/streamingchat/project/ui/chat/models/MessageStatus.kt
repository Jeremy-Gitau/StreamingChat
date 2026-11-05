package org.streamingchat.project.ui.chat.models

sealed interface MessageStatus {
    data object Streaming : MessageStatus

    data object Done : MessageStatus

    data object Stopped : MessageStatus

    data object CutOff : MessageStatus

    data class Failed(
        val reason: String,
    ) : MessageStatus
}
