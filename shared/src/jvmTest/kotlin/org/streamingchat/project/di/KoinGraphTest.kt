package org.streamingchat.project.di

import org.koin.core.context.stopKoin
import org.streamingchat.project.domain.model.StreamEngine
import org.streamingchat.project.domain.repositories.ChatRepository
import org.streamingchat.project.domain.repositories.SettingsRepository
import org.streamingchat.project.sources.network.ChatRemoteDataSource
import org.streamingchat.project.sources.network.EngineSwitchingRemoteDataSource
import org.streamingchat.project.sources.network.clients.NetworkConfig
import org.streamingchat.project.sources.network.koog.KoogChatRemoteDataSourceImpl
import org.streamingchat.project.sources.network.sse.SseChatRemoteDataSourceImpl
import org.streamingchat.project.ui.chat.ChatViewModel
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class KoinGraphTest {
    @AfterTest
    fun tearDown() = stopKoin()

    @Test
    fun resolvesEverythingTheAppNeeds() {
        val koin = initKoin().koin
        assertNotNull(koin.get<SettingsRepository>())
        assertNotNull(koin.get<ChatRemoteDataSource>())
        assertNotNull(koin.get<ChatRepository>())
        assertNotNull(koin.get<ChatViewModel>())
    }

    @Test
    fun bothEnginesResolve() {
        val koin = initKoin().koin
        assertNotNull(koin.get<SseChatRemoteDataSourceImpl>())
        assertNotNull(koin.get<KoogChatRemoteDataSourceImpl>())
    }

    @Test
    fun remoteIsTheEngineSwitcher() {
        val koin = initKoin().koin
        assertIs<EngineSwitchingRemoteDataSource>(koin.get<ChatRemoteDataSource>())
    }

    @Test
    fun sseEngineIsTheDefault() {
        val koin = initKoin().koin
        assertEquals(StreamEngine.Sse, koin.get<SettingsRepository>().settings.value.engine)
    }

    @Test
    fun startingEngineComesFromNetworkConfig() {
        val koin = initKoin(networkConfig = NetworkConfig(engine = StreamEngine.Koog)).koin
        assertEquals(StreamEngine.Koog, koin.get<SettingsRepository>().settings.value.engine)
    }
}
