package org.streamingchat.project

import androidx.compose.runtime.Composable
import org.streamingchat.project.ui.chat.ChatRoute
import org.streamingchat.project.ui.theme.AppTheme

@Composable
fun App() {
    AppTheme {
        ChatRoute()
    }
}
