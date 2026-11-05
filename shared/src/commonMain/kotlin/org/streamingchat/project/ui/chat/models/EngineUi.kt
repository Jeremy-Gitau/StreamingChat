package org.streamingchat.project.ui.chat.models

import org.streamingchat.project.domain.model.StreamEngine

val StreamEngine.label: String
    get() =
        when (this) {
            StreamEngine.Sse -> "SSE"
            StreamEngine.Koog -> "Koog"
        }

val StreamEngine.explanation: String
    get() =
        when (this) {
            StreamEngine.Sse -> "Hand built: Ktor bytes to lines to SSE events to ChatEvents."
            StreamEngine.Koog -> "JetBrains Koog streams the frames for you."
        }
