package org.streamingchat.project.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.streamingchat.project.domain.model.AiModel
import org.streamingchat.project.domain.model.ChatEvent
import org.streamingchat.project.domain.model.ChatSettings
import org.streamingchat.project.domain.model.FinishReason
import org.streamingchat.project.domain.model.Role
import org.streamingchat.project.domain.model.StreamEngine
import org.streamingchat.project.domain.repositories.ChatRepository
import org.streamingchat.project.domain.repositories.SettingsRepository
import org.streamingchat.project.ui.chat.models.MessageStatus
import org.streamingchat.project.ui.chat.models.UiMessage
import org.streamingchat.project.ui.chat.models.toHistory
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.TimeMark
import kotlin.time.TimeSource

data class ChatState(
    val messages: List<UiMessage> = emptyList(),
    val input: String = "",
    val isStreaming: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val settings: ChatSettings,
    val availableModels: List<AiModel>,
) {
    val canSend: Boolean get() = input.isNotBlank() && !isStreaming
    val canRegenerate: Boolean get() = !isStreaming && messages.lastOrNull()?.role == Role.Assistant
    val canChangeEngineOrModel: Boolean get() = !isStreaming
    val canClear: Boolean get() = !isStreaming && messages.isNotEmpty()
}

sealed interface ChatAction {
    data class InputChanged(
        val text: String,
    ) : ChatAction

    data object Send : ChatAction

    data object Stop : ChatAction

    data object Regenerate : ChatAction

    data object ClearConversation : ChatAction

    data class SendStarter(
        val prompt: String,
    ) : ChatAction

    data class CopyMessage(
        val id: Long,
    ) : ChatAction

    data object OpenSettings : ChatAction

    data object CloseSettings : ChatAction

    data class SelectModel(
        val model: AiModel,
    ) : ChatAction

    data class SelectEngine(
        val engine: StreamEngine,
    ) : ChatAction

    data class SystemPromptChanged(
        val text: String,
    ) : ChatAction

    data class MaxRetriesChanged(
        val maxRetries: Int,
    ) : ChatAction

    data class ShowThinkingLiveChanged(
        val enabled: Boolean,
    ) : ChatAction
}

sealed interface ChatUiEvent {
    data class ShowError(
        val message: String,
    ) : ChatUiEvent

    data class CopyToClipboard(
        val text: String,
    ) : ChatUiEvent
}

class ChatViewModel(
    private val repository: ChatRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val _state =
        MutableStateFlow(
            ChatState(
                settings = settingsRepository.settings.value,
                availableModels = repository.availableModels(),
            ),
        )
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private val _events = Channel<ChatUiEvent>(Channel.BUFFERED)
    val events: Flow<ChatUiEvent> = _events.receiveAsFlow()

    private var streamJob: Job? = null
    private var nextId = 0L

    init {
        // Settings are the source of truth; the state only mirrors the latest snapshot.
        settingsRepository.settings
            .onEach { settings -> updateState { copy(settings = settings) } }
            .launchIn(viewModelScope)
    }

    private fun updateState(reducer: ChatState.() -> ChatState) {
        _state.update { it.reducer() }
    }

    fun onAction(action: ChatAction) {
        when (action) {
            is ChatAction.InputChanged -> updateState { copy(input = action.text) }
            ChatAction.Send -> send()
            ChatAction.Stop -> stop()
            ChatAction.Regenerate -> regenerate()
            ChatAction.ClearConversation -> clearConversation()
            is ChatAction.SendStarter -> sendStarter(action.prompt)
            is ChatAction.CopyMessage -> copyMessage(action.id)
            ChatAction.OpenSettings -> updateState { copy(isSettingsOpen = true) }
            ChatAction.CloseSettings -> updateState { copy(isSettingsOpen = false) }
            is ChatAction.SelectModel -> changeEngineOrModel { copy(model = action.model) }
            is ChatAction.SelectEngine -> changeEngineOrModel { copy(engine = action.engine) }
            is ChatAction.SystemPromptChanged -> updateSettings { copy(systemPrompt = action.text) }
            is ChatAction.MaxRetriesChanged -> updateSettings { copy(maxRetries = action.maxRetries.coerceIn(0, 5)) }
            is ChatAction.ShowThinkingLiveChanged -> updateSettings { copy(showThinkingLive = action.enabled) }
        }
    }

    // Swapping engine or model mid reply would mix two sources in one answer, so it waits.
    private fun changeEngineOrModel(change: ChatSettings.() -> ChatSettings) {
        if (!state.value.canChangeEngineOrModel) return
        updateSettings(change)
    }

    // Mirror synchronously as well, so a text field bound to settings never lags a keystroke behind.
    private fun updateSettings(change: ChatSettings.() -> ChatSettings) {
        settingsRepository.update(change)
        updateState { copy(settings = settingsRepository.settings.value) }
    }

    private fun clearConversation() {
        if (!state.value.canClear) return
        updateState { copy(messages = emptyList(), isSettingsOpen = false) }
    }

    private fun sendStarter(prompt: String) {
        updateState { copy(input = prompt) }
        send()
    }

    private fun copyMessage(id: Long) {
        val text = state.value.messages.firstOrNull { it.id == id }?.text ?: return
        _events.trySend(ChatUiEvent.CopyToClipboard(text))
    }

    private fun send() {
        val current = state.value
        if (!current.canSend) return

        val userMessage = UiMessage(id = nextId++, role = Role.User, text = current.input.trim())
        updateState { copy(messages = messages + userMessage, input = "") }
        startReply()
    }

    private fun startReply() {
        val snapshot = state.value
        val history = snapshot.messages.toHistory()
        val model = snapshot.settings.model
        val replyId = nextId++

        updateState {
            copy(
                messages =
                    messages +
                        UiMessage(
                            replyId,
                            Role.Assistant,
                            text = "",
                            status = MessageStatus.Streaming,
                        ),
                isStreaming = true,
            )
        }

        streamJob =
            viewModelScope.launch {
                var startedAt = TimeSource.Monotonic.markNow()
                repository
                    .streamReply(model, history)
                    .onEach { event ->
                        if (event is ChatEvent.Retrying) startedAt = TimeSource.Monotonic.markNow() // backoff is not thinking
                        updateMessage(replyId) { it.reduce(event).stampThinkingTime(event, startedAt) }
                    }
                    .onCompletion { cause -> finishReply(replyId, cause) }
                    .catch { /* recorded by onCompletion; detailed errors come later */ }
                    .collect()
            }
    }

    private fun finishReply(
        id: Long,
        cause: Throwable?,
    ) {
        updateMessage(id) { message ->
            val status =
                when {
                    cause is CancellationException -> MessageStatus.Stopped

                    cause != null -> MessageStatus.Failed(cause.message ?: "Unknown error")

                    message.status != MessageStatus.Streaming -> message.status

                    // a Finished event set it
                    else -> MessageStatus.CutOff // ended without any Finished
                }
            message.copy(status = status, retryLabel = null) // a final status replaces "Retrying N of M"
        }
        updateState { copy(isStreaming = false) }

        if (cause != null && cause !is CancellationException) {
            _events.trySend(ChatUiEvent.ShowError(cause.message ?: "Something went wrong"))
        }
    }

    private fun updateMessage(
        id: Long,
        change: (UiMessage) -> UiMessage,
    ) {
        updateState { copy(messages = messages.map { if (it.id == id) change(it) else it }) }
    }

    private fun stop() {
        streamJob?.cancel()
    }

    private fun regenerate() {
        if (!state.value.canRegenerate) return
        updateState { copy(messages = messages.dropLast(1)) } // canRegenerate guarantees it's the assistant reply
        startReply()
    }
}

// "Thought for Ns": stamped once, on the first answer token after any thinking.
// Kept out of reduce so reduce stays a pure function of (message, event).
private fun UiMessage.stampThinkingTime(
    event: ChatEvent,
    startedAt: TimeMark,
): UiMessage =
    if (event is ChatEvent.Text && thinking.isNotEmpty() && thinkingMs == null) {
        copy(thinkingMs = startedAt.elapsedNow().inWholeMilliseconds)
    } else {
        this
    }

internal fun UiMessage.reduce(event: ChatEvent): UiMessage =
    when (event) {
        is ChatEvent.Thinking -> {
            copy(thinking = thinking + event.text, retryLabel = null)
        }

        is ChatEvent.Text -> {
            copy(text = text + event.text, retryLabel = null)
        }

        ChatEvent.ReclassifyTextAsThinking -> {
            copy(thinking = thinking + text, text = "")
        }

        is ChatEvent.Retrying -> {
            copy(retryLabel = "Retrying ${event.attempt} of ${event.maxRetries}…")
        }

        is ChatEvent.Finished -> {
            copy(
                status =
                    when (event.reason) {
                        FinishReason.Length, FinishReason.ContentFilter -> MessageStatus.CutOff
                        FinishReason.Stop, FinishReason.ToolCalls, FinishReason.Unknown -> MessageStatus.Done
                    },
            )
        }
    }
