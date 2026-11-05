package org.streamingchat.project.domain.model

enum class AiProvider { Ollama }

data class AiModel(
    val id: String,
    val displayName: String,
    val provider: AiProvider,
    val contextLength: Long,
    val supportsReasoning: Boolean,
)

object ModelCatalog {
    val NemotronSuper =
        AiModel(
            id = "nemotron-3-super:cloud",
            displayName = "Nemotron 3 Super",
            provider = AiProvider.Ollama,
            contextLength = 128_000,
            supportsReasoning = true,
        )

    val all: List<AiModel> = listOf(NemotronSuper)
}
