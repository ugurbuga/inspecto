package com.ugurbuga.inspecto

import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.statement.bodyAsText
import io.ktor.util.AttributeKey
import kotlin.time.TimeSource

private val RequestStartKey = AttributeKey<TimeSource.Monotonic.ValueTimeMark>("InspectoRequestStartTime")

/**
 * Ktor Client Plugin that automatically intercepts and logs HTTP requests and responses to Inspecto.
 */
val InspectoKtorLogging = createClientPlugin("InspectoKtorLogging") {
    onRequest { request, _ ->
        request.attributes.put(RequestStartKey, TimeSource.Monotonic.markNow())
    }

    on(Send) { request ->
        val startTime = TimeSource.Monotonic.markNow()
        val url = request.url.toString()
        val method = request.method.value
        val requestHeaders = request.headers.entries().associate { it.key to it.value.joinToString(", ") }
        val queryParams = request.url.parameters.entries().associate { it.key to it.value.joinToString(", ") }

        try {
            proceed(request)
        } catch (e: Throwable) {
            val duration = startTime.elapsedNow().inWholeMilliseconds
            Inspecto.addLog(
                url = url,
                method = method,
                statusCode = 0,
                durationMs = duration,
                requestHeaders = requestHeaders,
                requestQueryParams = queryParams,
                responseHeaders = emptyMap(),
                responseBody = e.message ?: e.toString(),
                tag = InspectoTag.KTOR
            )
            throw e
        }
    }

    onResponse { response ->
        val startTime = response.call.request.attributes.getOrNull(RequestStartKey)
        val duration = startTime?.elapsedNow()?.inWholeMilliseconds ?: 0L
        val url = response.call.request.url.toString()
        val method = response.call.request.method.value
        val statusCode = response.status.value

        val requestHeaders = response.call.request.headers.entries().associate { it.key to it.value.joinToString(", ") }
        val responseHeaders = response.headers.entries().associate { it.key to it.value.joinToString(", ") }
        val queryParams = response.call.request.url.parameters.entries().associate { it.key to it.value.joinToString(", ") }

        val responseBody = runCatching {
            response.bodyAsText()
        }.getOrNull()

        Inspecto.addLog(
            url = url,
            method = method,
            statusCode = statusCode,
            durationMs = duration,
            requestHeaders = requestHeaders,
            requestQueryParams = queryParams,
            responseHeaders = responseHeaders,
            responseBody = responseBody,
            tag = InspectoTag.KTOR
        )
    }
}
