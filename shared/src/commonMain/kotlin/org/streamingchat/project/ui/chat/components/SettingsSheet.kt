package org.streamingchat.project.ui.chat.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.streamingchat.project.domain.model.AiModel
import org.streamingchat.project.domain.model.ChatSettings
import org.streamingchat.project.domain.model.ModelCatalog
import org.streamingchat.project.domain.model.StreamEngine
import org.streamingchat.project.ui.chat.ChatAction
import org.streamingchat.project.ui.chat.ChatState
import org.streamingchat.project.ui.chat.models.explanation
import org.streamingchat.project.ui.chat.models.label
import org.streamingchat.project.ui.theme.AppIcons
import org.streamingchat.project.ui.theme.AppTheme
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    state: ChatState,
    onAction: (ChatAction) -> Unit,
) {
    // Works on desktop and mobile alike. No partial state, so it opens fully in one step.
    ModalBottomSheet(
        onDismissRequest = { onAction(ChatAction.CloseSettings) },
        sheetState = rememberBottomSheetState(SheetValue.Hidden, enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        SettingsContent(state, onAction)
    }
}

@Composable
fun SettingsContent(
    state: ChatState,
    onAction: (ChatAction) -> Unit,
) {
    val settings = state.settings
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text("Settings", style = MaterialTheme.typography.titleLarge)
        if (!state.canChangeEngineOrModel) LockedHint()
        EngineSelector(settings.engine, state.canChangeEngineOrModel) { onAction(ChatAction.SelectEngine(it)) }
        ModelSelector(settings.model, state.availableModels, state.canChangeEngineOrModel) {
            onAction(ChatAction.SelectModel(it))
        }
        OutlinedTextField(
            value = settings.systemPrompt.orEmpty(),
            onValueChange = { onAction(ChatAction.SystemPromptChanged(it)) },
            label = { Text("System prompt") },
            supportingText = { Text("Applies from the next reply.") },
            minLines = 2,
            maxLines = 4,
            modifier = Modifier.fillMaxWidth(),
        )
        RetrySlider(settings.maxRetries) { onAction(ChatAction.MaxRetriesChanged(it)) }
        ThinkingToggle(settings.showThinkingLive) { onAction(ChatAction.ShowThinkingLiveChanged(it)) }
        ClearConversationButton(state.canClear) { onAction(ChatAction.ClearConversation) }
        Spacer(Modifier.size(8.dp))
    }
}

@Composable
private fun LockedHint() {
    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.medium) {
        Text(
            "Engine and model are locked while a reply is streaming. Stop it or wait for it to finish.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(12.dp),
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EngineSelector(
    selected: StreamEngine,
    enabled: Boolean,
    onSelect: (StreamEngine) -> Unit,
) {
    val engines = StreamEngine.entries
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel("Engine")
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            engines.forEachIndexed { index, engine ->
                SegmentedButton(
                    selected = engine == selected,
                    onClick = { onSelect(engine) },
                    enabled = enabled,
                    shape = SegmentedButtonDefaults.itemShape(index, engines.size),
                ) { Text(engine.label) }
            }
        }
        Text(selected.explanation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ModelSelector(
    selected: AiModel,
    models: List<AiModel>,
    enabled: Boolean,
    onSelect: (AiModel) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        SectionLabel("Model")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            models.forEach { model ->
                FilterChip(
                    selected = model == selected,
                    onClick = { onSelect(model) },
                    enabled = enabled,
                    label = { Text(model.displayName) },
                )
            }
        }
    }
}

@Composable
private fun RetrySlider(
    maxRetries: Int,
    onChange: (Int) -> Unit,
) {
    Column {
        SectionLabel("Retries before giving up: $maxRetries")
        Slider(
            value = maxRetries.toFloat(),
            onValueChange = { onChange(it.roundToInt()) },
            valueRange = 0f..5f,
            steps = 4,
        )
        Text(
            "Only before the first token. Once text is on screen, a failure is final.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ThinkingToggle(
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("Show thinking while it streams", style = MaterialTheme.typography.bodyLarge)
            Text(
                "Off shows only a Thinking label until the answer starts.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun ClearConversationButton(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
    ) {
        Icon(AppIcons.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text("Clear conversation")
    }
}

@Preview
@Composable
private fun SettingsContentPreview() {
    AppTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
            SettingsContent(
                state =
                    ChatState(
                        isStreaming = true,
                        settings = ChatSettings(systemPrompt = "Be brief."),
                        availableModels = ModelCatalog.all,
                    ),
                onAction = {},
            )
        }
    }
}
