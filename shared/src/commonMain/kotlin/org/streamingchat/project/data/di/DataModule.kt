package org.streamingchat.project.data.di

import org.koin.dsl.module
import org.streamingchat.project.data.repositories.ChatRepositoryImpl
import org.streamingchat.project.data.repositories.InMemorySettingsRepository
import org.streamingchat.project.domain.model.ChatConfig
import org.streamingchat.project.domain.model.ChatSettings
import org.streamingchat.project.domain.repositories.ChatRepository
import org.streamingchat.project.domain.repositories.SettingsRepository
import org.streamingchat.project.sources.network.clients.NetworkConfig

val dataModule =
    module {
        single<SettingsRepository> {
            val chat = get<ChatConfig>()
            InMemorySettingsRepository(
                ChatSettings(
                    engine = get<NetworkConfig>().engine,
                    model = chat.defaultModel,
                    systemPrompt = chat.systemPrompt,
                    maxRetries = chat.retryPolicy.maxRetries,
                ),
            )
        }
        single<ChatRepository> {
            ChatRepositoryImpl(
                remote = get(),
                settings = get(),
                basePolicy = get<ChatConfig>().retryPolicy,
            )
        }
    }
