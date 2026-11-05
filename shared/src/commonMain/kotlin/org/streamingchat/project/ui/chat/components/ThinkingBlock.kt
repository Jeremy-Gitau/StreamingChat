package org.streamingchat.project.ui.chat.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.streamingchat.project.ui.theme.AppIcons
import org.streamingchat.project.ui.theme.AppTheme

@Composable
fun ThinkingBlock(
    thinking: String,
    isThinking: Boolean,
    thinkingMs: Long?,
    showLive: Boolean,
) {
    Column(Modifier.padding(bottom = 8.dp).animateContentSize()) {
        if (isThinking) {
            ThinkingLive(thinking, showLive)
        } else {
            ThoughtSummary(thinking, thinkingMs)
        }
    }
}

// While the model reasons: a pulsing label and, if enabled, the reasoning itself.
@Composable
private fun ThinkingLive(
    thinking: String,
    showLive: Boolean,
) {
    val pulse by rememberInfiniteTransition().animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
    )
    Text("Thinking…", style = MaterialTheme.typography.labelMedium, modifier = Modifier.alpha(pulse))
    if (showLive && thinking.isNotEmpty()) ThinkingText(thinking, live = true)
}

// After the answer starts: collapsed to one line, expandable.
@Composable
private fun ThoughtSummary(
    thinking: String,
    thinkingMs: Long?,
) {
    var expanded by remember { mutableStateOf(false) } // purely visual, so local to the composable
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { expanded = !expanded }.padding(vertical = 2.dp),
    ) {
        Text(
            text = thinkingMs?.let { "Thought for ${((it + 500) / 1000).coerceAtLeast(1)}s" } ?: "Thought",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Icon(
            imageVector = AppIcons.Expand,
            contentDescription = if (expanded) "Hide thinking" else "Show thinking",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp).rotate(if (expanded) 180f else 0f),
        )
    }
    if (expanded) ThinkingText(thinking, live = false)
}

@Composable
private fun ThinkingText(
    thinking: String,
    live: Boolean,
) {
    // reverseScrolling anchors the view to the bottom, so new reasoning stays in sight without any effect.
    val scroll = rememberScrollState()
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.padding(top = 6.dp),
    ) {
        Text(
            text = thinking,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
            modifier =
                Modifier
                    .heightIn(max = if (live) 120.dp else 320.dp)
                    .verticalScroll(scroll, reverseScrolling = live)
                    .padding(12.dp),
        )
    }
}

@Preview
@Composable
private fun ThinkingBlockPreview() {
    AppTheme {
        Surface {
            Column(Modifier.padding(16.dp)) {
                ThinkingBlock("The user wants a short greeting...", isThinking = true, thinkingMs = null, showLive = true)
                ThinkingBlock("The user wants a short greeting...", isThinking = false, thinkingMs = 4_200, showLive = true)
            }
        }
    }
}
