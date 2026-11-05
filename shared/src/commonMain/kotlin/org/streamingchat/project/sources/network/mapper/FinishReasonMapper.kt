package org.streamingchat.project.sources.network.mapper

import org.streamingchat.project.domain.model.FinishReason

fun String?.toFinishReason(): FinishReason =
    when (this) {
        "stop" -> FinishReason.Stop
        "length" -> FinishReason.Length
        "tool_calls" -> FinishReason.ToolCalls
        "content_filter" -> FinishReason.ContentFilter
        else -> FinishReason.Unknown
    }
