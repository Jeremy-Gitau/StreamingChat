package org.streamingchat.project.sources.network.clients

import io.ktor.client.HttpClient
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.utils.io.readLine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.streamingchat.project.sources.network.HttpStreamException

fun HttpClient.streamLines(
    url: String,
    body: String,
): Flow<String> =
    channelFlow {
        preparePost(url) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.execute { response ->
            if (!response.status.isSuccess()) {
                throw HttpStreamException(response.status.value, response.bodyAsText())
            }
            val channel = response.bodyAsChannel()
            while (true) {
                val line = channel.readLine() ?: break
                send(line)
            }
        }
    }
