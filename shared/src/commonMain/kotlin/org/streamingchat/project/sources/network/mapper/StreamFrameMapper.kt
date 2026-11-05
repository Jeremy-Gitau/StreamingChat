package org.streamingchat.project.sources.network.mapper

import ai.koog.prompt.streaming.StreamFrame
import org.streamingchat.project.domain.model.ChatEvent
import org.streamingchat.project.domain.model.FinishReason

fun StreamFrame.toChatEventOrNull(): ChatEvent? =
    when (this) {
        is StreamFrame.TextDelta -> text.takeIf { it.isNotEmpty() }?.let { ChatEvent.Text(it) }
        is StreamFrame.ReasoningDelta -> text?.takeIf { it.isNotEmpty() }?.let { ChatEvent.Thinking(it) }
        is StreamFrame.End -> ChatEvent.Finished(finishReason.toFinishReason())
        else -> null
    }
