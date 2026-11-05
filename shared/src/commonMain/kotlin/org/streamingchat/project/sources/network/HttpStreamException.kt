package org.streamingchat.project.sources.network

// Shared by both engines, so the repository has one error type to decide retries on.
class HttpStreamException(
    val status: Int,
    body: String,
) : Exception("HTTP $status: ${body.take(200)}")
