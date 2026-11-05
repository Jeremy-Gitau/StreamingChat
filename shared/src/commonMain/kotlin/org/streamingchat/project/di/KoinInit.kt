package org.streamingchat.project.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.streamingchat.project.data.di.dataModule
import org.streamingchat.project.domain.model.ChatConfig
import org.streamingchat.project.sources.network.clients.NetworkConfig
import org.streamingchat.project.sources.network.di.networkSourceModule
import org.streamingchat.project.ui.di.uiModule

fun initKoin(
    networkConfig: NetworkConfig = NetworkConfig(),
    chatConfig: ChatConfig = ChatConfig(),
    appDeclaration: KoinAppDeclaration = {},
): KoinApplication =
    startKoin {
        appDeclaration()
        modules(
            module {
                single { networkConfig }
                single { chatConfig }
            },
            networkSourceModule,
            dataModule,
            uiModule,
        )
    }
