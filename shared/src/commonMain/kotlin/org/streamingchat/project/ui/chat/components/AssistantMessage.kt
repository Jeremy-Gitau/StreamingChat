package org.streamingchat.project.ui.chat.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.streamingchat.project.domain.model.Role
import org.streamingchat.project.ui.chat.models.MessageStatus
import org.streamingchat.project.ui.chat.models.UiMessage
import org.streamingchat.project.ui.theme.AppIcons
import org.streamingchat.project.ui.theme.AppTheme

@Composable
fun AssistantMessage(
    message: UiMessage,
    showThinkingLive: Boolean,
    showActions: Boolean,
    onCopy: () -> Unit,
    onRegenerate: () -> Unit,
) {
    val streaming = message.status == MessageStatus.Streaming
    val isThinking = streaming && message.text.isEmpty()

    Column(Modifier.fillMaxWidth()) {
        message.retryLabel?.let { StatusPill(it, MaterialTheme.colorScheme.primary, Modifier.padding(bottom = 8.dp)) }

        if (message.thinking.isNotEmpty() || (isThinking && message.retryLabel == null)) {
            ThinkingBlock(message.thinking, isThinking, message.thinkingMs, showThinkingLive)
        }

        if (message.text.isNotEmpty()) AnswerText(message.text, streaming)

        if (!streaming && message.status != MessageStatus.Done) {
            Spacer(Modifier.height(8.dp))
            MessageStatusPill(message.status)
        }

        if (showActions) MessageActions(onCopy, onRegenerate, canCopy = message.text.isNotEmpty())
    }
}

// The cursor is part of the same Text, so it always sits right after the last token.
@Composable
private fun AnswerText(
    text: String,
    streaming: Boolean,
) {
    val cursorColor = MaterialTheme.colorScheme.primary
    Text(
        text =
            buildAnnotatedString {
                append(text)
                if (streaming) withStyle(SpanStyle(color = cursorColor)) { append(" ▍") }
            },
        style = MaterialTheme.typography.bodyLarge,
    )
}

@Composable
private fun MessageActions(
    onCopy: () -> Unit,
    onRegenerate: () -> Unit,
    canCopy: Boolean,
) {
    Row(Modifier.padding(top = 4.dp)) {
        if (canCopy) SmallAction(AppIcons.Copy, "Copy", onCopy)
        SmallAction(AppIcons.Regenerate, "Regenerate", onRegenerate)
    }
}

@Composable
private fun SmallAction(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) {
        Icon(icon, description, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Preview
@Composable
private fun AssistantMessagePreview() {
    AppTheme {
        Surface {
            Column(Modifier.padding(16.dp)) {
                AssistantMessage(
                    message =
                        UiMessage(
                            id = 1,
                            role = Role.Assistant,
                            text = "Kotlin Flow is a cold stream of values",
                            thinking = "They want two sentences.",
                            thinkingMs = 3_000,
                            status = MessageStatus.Streaming,
                        ),
                    showThinkingLive = true,
                    showActions = false,
                    onCopy = {},
                    onRegenerate = {},
                )
                AssistantMessage(
                    message = UiMessage(2, Role.Assistant, "Partial answer", status = MessageStatus.Stopped),
                    showThinkingLive = true,
                    showActions = true,
                    onCopy = {},
                    onRegenerate = {},
                )
            }
        }
    }
}
