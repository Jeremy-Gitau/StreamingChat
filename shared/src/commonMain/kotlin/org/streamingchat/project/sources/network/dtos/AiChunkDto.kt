package org.streamingchat.project.sources.network.dtos

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ChunkDto(
    val choices: List<ChoiceDto> = emptyList(),
)

@Serializable
internal data class ChoiceDto(
    val delta: DeltaDto = DeltaDto(),
    @SerialName("finish_reason") val finishReason: String? = null,
)

@Serializable
internal data class DeltaDto(
    val content: String? = null,
    val reasoning: String? = null, // Ollama
    @SerialName("reasoning_content") val reasoningContent: String? = null, // DeepSeek and others
)
