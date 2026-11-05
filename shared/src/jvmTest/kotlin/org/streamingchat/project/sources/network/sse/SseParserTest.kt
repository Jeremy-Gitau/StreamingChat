package org.streamingchat.project.sources.network.sse

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SseParserTest {
    @Test
    fun parsesDataEventsSeparatedByBlankLines() =
        runTest {
            val events = flowOf("data: hello", "", "data: world", "").parseSse().toList()
            assertEquals(listOf(SseEvent(null, "hello"), SseEvent(null, "world")), events)
        }

    @Test
    fun joinsMultipleDataLinesAndReadsEventType() =
        runTest {
            val events =
                flowOf("event: content_block_delta", "data: line one", "data: line two", "")
                    .parseSse()
                    .toList()
            assertEquals(listOf(SseEvent("content_block_delta", "line one\nline two")), events)
        }

    @Test
    fun ignoresCommentsAndKeepalives() =
        runTest {
            val events = flowOf(": keepalive", "", "data: real", "").parseSse().toList()
            assertEquals(listOf(SseEvent(null, "real")), events)
        }

    @Test
    fun stripsOnlyOneLeadingSpace() =
        runTest {
            val events = flowOf("data:  two spaces", "").parseSse().toList()
            assertEquals(listOf(SseEvent(null, " two spaces")), events)
        }

    @Test
    fun discardsUnfinishedEventAtEndOfStream() =
        runTest {
            val events = flowOf("data: complete", "", "data: never finished").parseSse().toList()
            assertEquals(listOf(SseEvent(null, "complete")), events)
        }
}
