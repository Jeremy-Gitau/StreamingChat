package org.streamingchat.project.ui.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.streamingchat.project.ui.chat.ChatViewModel

val uiModule = module {
    viewModelOf(::ChatViewModel)
}