package org.streamingchat.project.ui.chat.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.streamingchat.project.ui.chat.models.StarterPrompt
import org.streamingchat.project.ui.chat.models.demoStarters
import org.streamingchat.project.ui.theme.AppTheme

@Composable
fun EmptyState(
    starters: List<StarterPrompt>,
    onStarter: (StarterPrompt) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("What should we stream?", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Tap a prompt to start.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
        )
        Column(Modifier.widthIn(max = 480.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            starters.forEach { StarterCard(it, onClick = { onStarter(it) }) }
        }
    }
}

@Composable
private fun StarterCard(
    starter: StarterPrompt,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(starter.title, style = MaterialTheme.typography.titleSmall)
            Text(
                starter.prompt,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview
@Composable
private fun EmptyStatePreview() {
    AppTheme { Surface { EmptyState(demoStarters, onStarter = {}) } }
}
