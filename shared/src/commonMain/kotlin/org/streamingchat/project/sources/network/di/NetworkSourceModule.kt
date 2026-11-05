package org.streamingchat.project.sources.network.di

import ai.koog.prompt.executor.ollama.client.OllamaClient
import io.ktor.client.HttpClient
import org.koin.dsl.module
import org.koin.dsl.onClose
import org.streamingchat.project.domain.model.AiProvider
import org.streamingchat.project.sources.network.ChatRemoteDataSource
import org.streamingchat.project.sources.network.EngineSwitchingRemoteDataSource
import org.streamingchat.project.sources.network.clients.HttpClientFactory
import org.streamingchat.project.sources.network.clients.KoogClientFactory
import org.streamingchat.project.sources.network.koog.KoogChatRemoteDataSourceImpl
import org.streamingchat.project.sources.network.sse.SseChatRemoteDataSourceImpl

val networkSourceModule =
    module {
        single<HttpClient> { HttpClientFactory.create(config = get()) } onClose { it?.close() }
        single { KoogClientFactory.httpFactory() }
        single<OllamaClient> {
            KoogClientFactory.ollama(config = get(), httpFactory = get())
        } onClose { it?.close() }

        // Both engines are bound by their concrete type; only the switcher is the ChatRemoteDataSource.
        single { SseChatRemoteDataSourceImpl(client = get(), config = get()) }
        single { KoogChatRemoteDataSourceImpl(clients = mapOf(AiProvider.Ollama to get<OllamaClient>())) }

        single<ChatRemoteDataSource> {
            EngineSwitchingRemoteDataSource(
                settings = get(),
                sse = get<SseChatRemoteDataSourceImpl>(),
                koog = lazy { get<KoogChatRemoteDataSourceImpl>() },
            )
        }
    }
