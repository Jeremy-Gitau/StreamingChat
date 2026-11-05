package org.streamingchat.project.domain.repositories

import kotlinx.coroutines.flow.StateFlow
import org.streamingchat.project.domain.model.ChatSettings

interface SettingsRepository {
    val settings: StateFlow<ChatSettings>

    fun update(transform: ChatSettings.() -> ChatSettings)
}
