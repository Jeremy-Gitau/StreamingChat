package org.streamingchat.project

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.streamingchat.project.di.initKoin

fun main() {
    initKoin() // the starting engine comes from NetworkConfig; switch it at runtime in the app
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "StreamingChat",
        ) {
            App()
        }
    }
}
