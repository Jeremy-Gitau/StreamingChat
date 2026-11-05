package org.streamingchat.project.ui.chat.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.streamingchat.project.domain.model.Role
import org.streamingchat.project.ui.chat.models.UiMessage

@Composable
fun MessageList(
    messages: List<UiMessage>,
    showThinkingLive: Boolean,
    canRegenerate: Boolean,
    onCopy: (Long) -> Unit,
    onRegenerate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lastId = messages.lastOrNull()?.id
    // reverseLayout pins the list to the bottom: new tokens grow upward from the input bar.
    LazyColumn(
        modifier = modifier,
        reverseLayout = true,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Stable keys: a new token updates one item; the rest are skipped.
        items(items = messages.asReversed(), key = { it.id }, contentType = { it.role }) { message ->
            when (message.role) {
                Role.User -> {
                    UserBubble(message.text)
                }

                Role.Assistant, Role.System -> {
                    AssistantMessage(
                        message = message,
                        showThinkingLive = showThinkingLive,
                        showActions = canRegenerate && message.id == lastId,
                        onCopy = { onCopy(message.id) },
                        onRegenerate = onRegenerate,
                    )
                }
            }
        }
    }
}
