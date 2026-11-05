package org.streamingchat.project.data.repositories

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.streamingchat.project.domain.model.ChatSettings
import org.streamingchat.project.domain.repositories.SettingsRepository

class InMemorySettingsRepository(
    initial: ChatSettings = ChatSettings(),
) : SettingsRepository {
    private val _settings = MutableStateFlow(initial)
    override val settings: StateFlow<ChatSettings> = _settings.asStateFlow()

    override fun update(transform: ChatSettings.() -> ChatSettings) {
        _settings.update { it.transform() }
    }
}
