package org.streamingchat.project.sources.network.mapper

import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import org.streamingchat.project.domain.model.AiModel
import org.streamingchat.project.domain.model.AiProvider

fun AiModel.toKoogModel(): LLModel =
    LLModel(
        provider = provider.toKoogProvider(),
        id = id,
        capabilities = listOf(LLMCapability.Temperature, LLMCapability.Completion),
        contextLength = contextLength,
    )

fun AiProvider.toKoogProvider(): LLMProvider =
    when (this) {
        AiProvider.Ollama -> LLMProvider.Ollama
    }
