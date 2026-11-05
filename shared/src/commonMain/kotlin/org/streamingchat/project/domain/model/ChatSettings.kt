package org.streamingchat.project.domain.model

// Which implementation streams the reply. The domain only knows the choice, not Ktor or Koog.
enum class StreamEngine { Sse, Koog }

data class ChatSettings(
    val engine: StreamEngine = StreamEngine.Sse,
    val model: AiModel = ModelCatalog.NemotronSuper,
    val systemPrompt: String? = null,
    val maxRetries: Int = RetryPolicy().maxRetries,
    val showThinkingLive: Boolean = true,
)
