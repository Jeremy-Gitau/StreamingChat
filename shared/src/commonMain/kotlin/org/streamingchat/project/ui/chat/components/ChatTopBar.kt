package org.streamingchat.project.ui.chat.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.streamingchat.project.domain.model.AiModel
import org.streamingchat.project.domain.model.ModelCatalog
import org.streamingchat.project.domain.model.StreamEngine
import org.streamingchat.project.ui.chat.models.label
import org.streamingchat.project.ui.theme.AppIcons
import org.streamingchat.project.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(
    engine: StreamEngine,
    model: AiModel,
    onOpenSettings: () -> Unit,
) {
    TopAppBar(
        title = { Text("StreamingChat", style = MaterialTheme.typography.titleMedium) },
        actions = {
            EngineChip(engine, model, onOpenSettings)
            IconButton(onClick = onOpenSettings) { Icon(AppIcons.Settings, contentDescription = "Settings") }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
    )
}

// Always shows which engine is live, so the audience can see the switch.
@Composable
private fun EngineChip(
    engine: StreamEngine,
    model: AiModel,
    onClick: () -> Unit,
) {
    AssistChip(
        onClick = onClick,
        shape = CircleShape,
        colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = null,
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(engine.label, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                Text(
                    "  ${model.displayName}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 140.dp),
                )
            }
        },
        leadingIcon = {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(6.dp)) {}
        },
        modifier = Modifier.padding(end = 4.dp),
    )
}

@Preview
@Composable
private fun ChatTopBarPreview() {
    AppTheme { ChatTopBar(StreamEngine.Sse, ModelCatalog.NemotronSuper, onOpenSettings = {}) }
}
