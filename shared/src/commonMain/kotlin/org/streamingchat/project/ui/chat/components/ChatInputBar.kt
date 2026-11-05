package org.streamingchat.project.ui.chat.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.streamingchat.project.ui.theme.AppIcons
import org.streamingchat.project.ui.theme.AppTheme

@Composable
fun ChatInputBar(
    input: String,
    canSend: Boolean,
    isStreaming: Boolean,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            PillTextField(input, onInputChange, onSend, Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            SendOrStopButton(canSend, isStreaming, onSend, onStop)
        }
    }
}

// Enter sends, Shift+Enter is a new line. Typing keeps working while streaming; only sending is blocked.
@Composable
private fun PillTextField(
    input: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = input,
        onValueChange = onInputChange,
        placeholder = { Text("Ask anything") },
        maxLines = 5,
        shape = RoundedCornerShape(24.dp),
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
        modifier =
            modifier.onPreviewKeyEvent { event ->
                val isEnter = event.key == Key.Enter || event.key == Key.NumPadEnter
                if (!isEnter || event.isShiftPressed) return@onPreviewKeyEvent false
                if (event.type == KeyEventType.KeyDown) onSend() // the ViewModel ignores it if sending is blocked
                true
            },
    )
}

@Composable
private fun SendOrStopButton(
    canSend: Boolean,
    isStreaming: Boolean,
    onSend: () -> Unit,
    onStop: () -> Unit,
) {
    FilledIconButton(
        onClick = if (isStreaming) onStop else onSend,
        enabled = isStreaming || canSend,
        colors =
            IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        modifier = Modifier.padding(bottom = 4.dp),
    ) {
        if (isStreaming) {
            Icon(AppIcons.Stop, contentDescription = "Stop")
        } else {
            Icon(AppIcons.Send, contentDescription = "Send")
        }
    }
}

@Preview
@Composable
private fun ChatInputBarPreview() {
    AppTheme {
        ChatInputBar("Explain Flow", canSend = true, isStreaming = false, onInputChange = {}, onSend = {}, onStop = {})
    }
}
