package org.streamingchat.project.ui.chat.models

data class StarterPrompt(
    val title: String,
    val prompt: String,
)

// Picked for the live demo: a quick one, a long one to Stop, and one that sets up a follow up.
val demoStarters =
    listOf(
        StarterPrompt("Quick hello", "Say hi to the droidcon Kenya audience in one sentence."),
        StarterPrompt("Explain Flow", "Explain Kotlin Flow in two sentences."),
        StarterPrompt(
            "Long answer (try Stop)",
            "Ignore any word limit. Write a detailed 800 word story about a Nairobi developer " +
                "who builds a streaming chat app with Kotlin Flow.",
        ),
        StarterPrompt(
            "Remember this (then ask a follow up)",
            "I'm giving a talk at droidcon Kenya about streaming AI responses. Remember that, and wish me luck.",
        ),
    )
