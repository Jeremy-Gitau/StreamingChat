package org.streamingchat.project.sources.network.sse

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.streamingchat.project.domain.model.ChatEvent
import org.streamingchat.project.domain.model.FinishReason
import org.streamingchat.project.sources.network.mapper.toChatEvents
import kotlin.test.Test
import kotlin.test.assertEquals

class OpenAiChunkMapperTest {
    private fun sse(json: String) = SseEvent(event = null, data = json)

    @Test
    fun reasoningBecomesThinkingAndEmptyContentIsIgnored() =
        runTest {
            val events =
                flowOf(
                    sse("""{"choices":[{"index":0,"delta":{"role":"assistant","content":"","reasoning":"We"},"finish_reason":null}]}"""),
                ).toChatEvents().toList()

            assertEquals(listOf(ChatEvent.Thinking("We")), events)
        }

    @Test
    fun contentBecomesText() =
        runTest {
            val events =
                flowOf(
                    sse("""{"choices":[{"index":0,"delta":{"content":"Hi there everyone"},"finish_reason":null}]}"""),
                ).toChatEvents().toList()

            assertEquals(listOf(ChatEvent.Text("Hi there everyone")), events)
        }

    @Test
    fun finishReasonBecomesFinished() =
        runTest {
            val events =
                flowOf(
                    sse("""{"choices":[{"index":0,"delta":{},"finish_reason":"stop"}]}"""),
                ).toChatEvents().toList()

            assertEquals(listOf(ChatEvent.Finished(FinishReason.Stop)), events)
        }

    @Test
    fun unknownFieldsAreIgnored() =
        runTest {
            val events =
                flowOf(
                    sse("""{"id":"x","system_fingerprint":"fp","brand_new_field":42,"choices":[{"delta":{"content":"ok"}}]}"""),
                ).toChatEvents().toList()

            assertEquals(listOf(ChatEvent.Text("ok")), events)
        }
}
