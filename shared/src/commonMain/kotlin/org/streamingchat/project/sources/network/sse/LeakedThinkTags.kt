package org.streamingchat.project.sources.network.sse

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.streamingchat.project.domain.model.ChatEvent

private const val CLOSE_TAG = "</think>"

fun Flow<ChatEvent>.handleLeakedThinkTags(): Flow<ChatEvent> =
    flow {
        val pending = StringBuilder()
        var tagSeen = false

        collect { event ->
            if (event !is ChatEvent.Text || tagSeen) {
                if (event is ChatEvent.Finished && pending.isNotEmpty()) {
                    emit(ChatEvent.Text(pending.toString()))
                    pending.clear()
                }
                emit(event)
                return@collect
            }

            pending.append(event.text)
            val tagIndex = pending.indexOf(CLOSE_TAG)

            if (tagIndex >= 0) {
                val beforeTag = pending.substring(0, tagIndex)
                val afterTag = pending.substring(tagIndex + CLOSE_TAG.length).trimStart()
                pending.clear()
                tagSeen = true

                emit(ChatEvent.ReclassifyTextAsThinking)
                if (beforeTag.isNotEmpty()) emit(ChatEvent.Thinking(beforeTag))
                if (afterTag.isNotEmpty()) emit(ChatEvent.Text(afterTag))
            } else {
                val hold = pending.partialTagLength()
                val safe = pending.substring(0, pending.length - hold)
                if (safe.isNotEmpty()) emit(ChatEvent.Text(safe))
                val retained = pending.substring(pending.length - hold)
                pending.clear()
                pending.append(retained)
            }
        }

        if (pending.isNotEmpty()) emit(ChatEvent.Text(pending.toString()))
    }

private fun CharSequence.partialTagLength(): Int {
    for (k in minOf(CLOSE_TAG.length - 1, length) downTo 1) {
        if (endsWith(CLOSE_TAG.substring(0, k))) return k
    }
    return 0
}
