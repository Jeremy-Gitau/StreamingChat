package org.streamingchat.project.sources.network.sse

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class SseEvent(
    val event: String?,
    val data: String,
)

fun Flow<String>.parseSse(): Flow<SseEvent> =
    flow {
        var eventType: String? = null
        val data = StringBuilder()
        var hasData = false

        suspend fun dispatch() {
            if (hasData) emit(SseEvent(eventType, data.toString()))
            eventType = null
            data.clear()
            hasData = false
        }

        collect { line ->
            when {
                line.isEmpty() -> {
                    dispatch()
                }

                line.startsWith(":") -> {
                    // comment or keepalive: ignored
                }

                else -> {
                    val colon = line.indexOf(':')
                    val field = if (colon == -1) line else line.substring(0, colon)
                    var value = if (colon == -1) "" else line.substring(colon + 1)
                    if (value.startsWith(" ")) value = value.substring(1)

                    when (field) {
                        "event" -> {
                            eventType = value
                        }

                        "data" -> {
                            if (hasData) data.append('\n')
                            data.append(value)
                            hasData = true
                        }
                    }
                }
            }
        }
        // Stream ended without a final blank line: the SSE spec says
        // an incomplete event is discarded, so we do NOT dispatch here.
    }
