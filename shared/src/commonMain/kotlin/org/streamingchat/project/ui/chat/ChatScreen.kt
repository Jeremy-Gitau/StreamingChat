package org.streamingchat.project.ui.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.streamingchat.project.domain.model.ChatSettings
import org.streamingchat.project.domain.model.ModelCatalog
import org.streamingchat.project.domain.model.Role
import org.streamingchat.project.ui.chat.components.ChatInputBar
import org.streamingchat.project.ui.chat.components.ChatTopBar
import org.streamingchat.project.ui.chat.components.EmptyState
import org.streamingchat.project.ui.chat.components.MessageList
import org.streamingchat.project.ui.chat.components.SettingsSheet
import org.streamingchat.project.ui.chat.models.MessageStatus
import org.streamingchat.project.ui.chat.models.UiMessage
import org.streamingchat.project.ui.chat.models.demoStarters
import org.streamingchat.project.ui.theme.AppTheme

@Suppress("DEPRECATION") // LocalClipboardManager: the replacement needs a platform ClipEntry per target
@Composable
fun ChatRoute() {
    val viewModel = koinViewModel<ChatViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboard = LocalClipboardManager.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is ChatUiEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }

                is ChatUiEvent.CopyToClipboard -> {
                    clipboard.setText(AnnotatedString(event.text))
                    snackbarHostState.showSnackbar("Copied")
                }
            }
        }
    }

    ChatScreen(state = state, onAction = viewModel::onAction, snackbarHostState = snackbarHostState)
}

@Composable
fun ChatScreen(
    state: ChatState,
    onAction: (ChatAction) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ChatTopBar(state.settings.engine, state.settings.model, onOpenSettings = { onAction(ChatAction.OpenSettings) })
        },
        bottomBar = {
            ChatInputBar(
                input = state.input,
                canSend = state.canSend,
                isStreaming = state.isStreaming,
                onInputChange = { onAction(ChatAction.InputChanged(it)) },
                onSend = { onAction(ChatAction.Send) },
                onStop = { onAction(ChatAction.Stop) },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        // A centered reading column on wide desktop windows, full width on phones.
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            val content = Modifier.fillMaxSize().widthIn(max = 760.dp)
            if (state.messages.isEmpty()) {
                EmptyState(demoStarters, onStarter = { onAction(ChatAction.SendStarter(it.prompt)) }, modifier = content)
            } else {
                MessageList(
                    messages = state.messages,
                    showThinkingLive = state.settings.showThinkingLive,
                    canRegenerate = state.canRegenerate,
                    onCopy = { onAction(ChatAction.CopyMessage(it)) },
                    onRegenerate = { onAction(ChatAction.Regenerate) },
                    modifier = content,
                )
            }
        }
    }

    if (state.isSettingsOpen) SettingsSheet(state, onAction)
}

@Preview
@Composable
private fun ChatScreenPreview() {
    AppTheme {
        ChatScreen(
            state =
                ChatState(
                    messages =
                        listOf(
                            UiMessage(0, Role.User, "Explain Kotlin Flow in two sentences."),
                            UiMessage(
                                id = 1,
                                role = Role.Assistant,
                                text = "Kotlin Flow is a cold, asynchronous stream",
                                thinking = "Two sentences, keep it simple.",
                                thinkingMs = 2_000,
                                status = MessageStatus.Streaming,
                            ),
                        ),
                    isStreaming = true,
                    settings = ChatSettings(),
                    availableModels = ModelCatalog.all,
                ),
            onAction = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
