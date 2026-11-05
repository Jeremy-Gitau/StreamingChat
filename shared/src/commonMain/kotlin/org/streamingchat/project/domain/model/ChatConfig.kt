package org.streamingchat.project.domain.model

data class RetryPolicy(
    val maxRetries: Int = 3,
    val initialDelayMs: Long = 1_000,
    val maxDelayMs: Long = 8_000,
) {
    // 1s, 2s, 4s, capped: exponential backoff. The shift is capped so a large retry count cannot overflow.
    fun delayFor(attempt: Long): Long = (initialDelayMs shl attempt.coerceAtMost(20).toInt()).coerceAtMost(maxDelayMs)
}

data class ChatConfig(
    val systemPrompt: String? = "You are a concise assistant. Keep answers under 120 words.",
    val defaultModel: AiModel = ModelCatalog.NemotronSuper,
    val retryPolicy: RetryPolicy = RetryPolicy(),
)
