package org.streamingchat.project.ui.chat.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.streamingchat.project.ui.chat.models.MessageStatus
import org.streamingchat.project.ui.theme.AppTheme

@Composable
fun StatusPill(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Surface(shape = CircleShape, color = color.copy(alpha = 0.14f), modifier = modifier) {
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

// Final statuses only. Streaming and Done need no pill.
@Composable
fun MessageStatusPill(status: MessageStatus) {
    val colors = MaterialTheme.colorScheme
    when (status) {
        MessageStatus.Stopped -> StatusPill("Stopped", colors.secondary)
        MessageStatus.CutOff -> StatusPill("Cut off", colors.tertiary)
        is MessageStatus.Failed -> StatusPill("Failed: ${status.reason}", colors.error)
        MessageStatus.Streaming, MessageStatus.Done -> Unit
    }
}

@Preview
@Composable
private fun StatusPillPreview() {
    AppTheme {
        Surface {
            MessageStatusPill(MessageStatus.Failed("HTTP 503"))
        }
    }
}
