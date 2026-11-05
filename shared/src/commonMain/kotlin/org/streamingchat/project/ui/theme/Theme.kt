package org.streamingchat.project.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Toned charcoal, never pure black, with a single teal accent.
private val AppColors =
    darkColorScheme(
        primary = Color(0xFF4FD1C5),
        onPrimary = Color(0xFF00201D),
        primaryContainer = Color(0xFF123B38),
        onPrimaryContainer = Color(0xFFB4F1E9),
        secondary = Color(0xFF9AA1AD),
        onSecondary = Color(0xFF16181D),
        tertiary = Color(0xFFF2C46D), // amber, used only for "Cut off"
        background = Color(0xFF111318),
        onBackground = Color(0xFFE6E8EE),
        surface = Color(0xFF111318),
        onSurface = Color(0xFFE6E8EE),
        surfaceVariant = Color(0xFF22262D),
        onSurfaceVariant = Color(0xFF9AA1AD),
        surfaceContainerLowest = Color(0xFF0D0F13),
        surfaceContainerLow = Color(0xFF171A1F),
        surfaceContainer = Color(0xFF1B1E24),
        surfaceContainerHigh = Color(0xFF22262D),
        surfaceContainerHighest = Color(0xFF2A2F37),
        outline = Color(0xFF3A404B),
        outlineVariant = Color(0xFF2A2F37),
        error = Color(0xFFFF8A80),
        onError = Color(0xFF3B0907),
    )

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = AppColors, content = content)
}
