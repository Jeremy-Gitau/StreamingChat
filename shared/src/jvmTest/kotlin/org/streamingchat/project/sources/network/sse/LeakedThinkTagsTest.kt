package org.streamingchat.project.sources.network.sse

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.streamingchat.project.domain.model.ChatEvent
import org.streamingchat.project.domain.model.ChatEvent.*
import kotlin.test.Test
import kotlin.test.assertEquals

class LeakedThinkTagsTest {
    @Test
    fun normalTextPassesThroughUnchanged() =
        runTest {
            val events = flowOf(Text("Hello"), Text(" world")).handleLeakedThinkTags().toList()
            assertEquals(listOf<ChatEvent>(Text("Hello"), Text(" world")), events)
        }

    @Test
    fun tagInOneDeltaReclassifiesTheDraft() =
        runTest {
            val events = flowOf(Text("draft answer</think>\nreal answer")).handleLeakedThinkTags().toList()
            assertEquals(
                listOf(ReclassifyTextAsThinking, Thinking("draft answer"), Text("real answer")),
                events,
            )
        }

    @Test
    fun tagSplitAcrossDeltasIsStillCaught() =
        runTest {
            val events = flowOf(Text("draft</th"), Text("ink>\nreal")).handleLeakedThinkTags().toList()
            assertEquals(
                listOf(Text("draft"), ReclassifyTextAsThinking, Text("real")),
                events,
            )
        }

    @Test
    fun heldBackCharactersAreReleasedIfNoTagFollows() =
        runTest {
            val events = flowOf(Text("a < b"), Text(" is true")).handleLeakedThinkTags().toList()
            assertEquals("a < b is true", events.filterIsInstance<Text>().joinToString("") { it.text })
        }
}
