package io.github.ugurbuga.inspecto

import kotlinx.serialization.Serializable

@Serializable
data class InspectoLogEntry(
    val id: String = "",
    val url: String = "",
    val method: String = InspectoMethod.GET,
    val statusCode: Int = 200,
    val durationMs: Long = 0L,
    val timestampMs: Long = 0L,
    val requestHeaders: Map<String, String> = emptyMap(),
    val requestQueryParams: Map<String, String> = emptyMap(),
    val requestBody: String? = null,
    val responseHeaders: Map<String, String> = emptyMap(),
    val responseBody: String? = null,
    val responseSizeByte: Long = 0L,
    val tag: String = InspectoTag.DEFAULT
) {
    val isSuccess: Boolean
        get() = statusCode in 200..299

    val isError: Boolean
        get() = statusCode >= 400 || statusCode == 0

    val path: String
        get() = runCatching {
            val withoutProtocol = url.substringAfter("://")
            "/" + withoutProtocol.substringAfter("/", "")
        }.getOrDefault(url)

    val host: String
        get() = runCatching {
            val withoutProtocol = url.substringAfter("://")
            withoutProtocol.substringBefore("/")
        }.getOrDefault("")

    val formattedSize: String
        get() {
            val bytes = if (responseSizeByte > 0) responseSizeByte else (responseBody?.length?.toLong() ?: 0L)
            return when {
                bytes <= 0L -> "0 B"
                bytes < 1024L -> "$bytes B"
                bytes < 1024L * 1024L -> {
                    val kb = bytes / 1024.0
                    val formatted = (kb * 10).toLong() / 10.0
                    if (formatted % 1.0 == 0.0) "${formatted.toLong()} KB" else "$formatted KB"
                }
                else -> {
                    val mb = bytes / (1024.0 * 1024.0)
                    val formatted = (mb * 10).toLong() / 10.0
                    if (formatted % 1.0 == 0.0) "${formatted.toLong()} MB" else "$formatted MB"
                }
            }
        }

    fun toCurlCommand(): String {
        val builder = StringBuilder("curl -X $method \"$url\"")
        requestHeaders.forEach { (key, value) ->
            builder.append(" -H \"$key: $value\"")
        }
        if (!requestBody.isNullOrBlank()) {
            builder.append(" -d '${requestBody.replace("'", "'\\''")}'")
        }
        return builder.toString()
    }

    /**
     * Formats the Overview, Request, and Response into a single stacked plain text representation.
     */
    fun toFormattedLogText(prettyResponseBody: String? = null): String {
        val builder = StringBuilder()
        builder.appendLine("========================================")
        builder.appendLine("INSPECTO")
        builder.appendLine("========================================")

        builder.appendLine("[OVERVIEW]")
        builder.appendLine("URL: $url")
        builder.appendLine("Method: $method")
        builder.appendLine("Status: $statusCode")
        builder.appendLine("Duration: ${durationMs}ms")
        builder.appendLine("Size: $formattedSize")
        builder.appendLine()

        builder.appendLine("[REQUEST]")
        builder.appendLine("Headers:")
        if (requestHeaders.isEmpty()) {
            builder.appendLine("  (None)")
        } else {
            requestHeaders.forEach { (k, v) -> builder.appendLine("  $k: $v") }
        }
        builder.appendLine("Query Parameters:")
        if (requestQueryParams.isEmpty()) {
            builder.appendLine("  (None)")
        } else {
            requestQueryParams.forEach { (k, v) -> builder.appendLine("  $k=$v") }
        }
        builder.appendLine("Body:")
        if (requestBody.isNullOrBlank()) {
            builder.appendLine("  (Empty)")
        } else {
            builder.appendLine(requestBody.prependIndent("  "))
        }
        builder.appendLine()

        builder.appendLine("[RESPONSE]")
        builder.appendLine("Headers:")
        if (responseHeaders.isEmpty()) {
            builder.appendLine("  (None)")
        } else {
            responseHeaders.forEach { (k, v) -> builder.appendLine("  $k: $v") }
        }
        builder.appendLine("Body:")
        val bodyToUse = prettyResponseBody ?: responseBody
        if (bodyToUse.isNullOrBlank()) {
            builder.appendLine("  (Empty)")
        } else {
            builder.appendLine(bodyToUse.prependIndent("  "))
        }
        builder.appendLine("========================================")

        return builder.toString()
    }

    fun formattedRequestHeaders(): String {
        if (requestHeaders.isEmpty()) return ""
        return requestHeaders.entries.joinToString("\n") { "${it.key}: ${it.value}" }
    }

    fun formattedQueryParams(): String {
        if (requestQueryParams.isEmpty()) return ""
        return requestQueryParams.entries.joinToString("\n") { "${it.key}=${it.value}" }
    }

    fun formattedResponseHeaders(): String {
        if (responseHeaders.isEmpty()) return ""
        return responseHeaders.entries.joinToString("\n") { "${it.key}: ${it.value}" }
    }

    fun formattedOverview(): String {
        return """
            URL: $url
            Method: $method
            Status: $statusCode
            Duration: ${durationMs}ms
            Size: $formattedSize
        """.trimIndent()
    }

    fun formattedRequestSection(): String {
        val builder = StringBuilder()
        builder.appendLine("[REQUEST]")
        builder.appendLine("Headers:")
        if (requestHeaders.isEmpty()) {
            builder.appendLine("  (None)")
        } else {
            requestHeaders.forEach { (k, v) -> builder.appendLine("  $k: $v") }
        }
        builder.appendLine("Query Parameters:")
        if (requestQueryParams.isEmpty()) {
            builder.appendLine("  (None)")
        } else {
            requestQueryParams.forEach { (k, v) -> builder.appendLine("  $k=$v") }
        }
        builder.appendLine("Body:")
        if (requestBody.isNullOrBlank()) {
            builder.appendLine("  (Empty)")
        } else {
            builder.appendLine(requestBody.prependIndent("  "))
        }
        return builder.toString().trimEnd()
    }

    fun formattedResponseSection(prettyResponseBody: String? = null): String {
        val builder = StringBuilder()
        builder.appendLine("[RESPONSE]")
        builder.appendLine("Headers:")
        if (responseHeaders.isEmpty()) {
            builder.appendLine("  (None)")
        } else {
            responseHeaders.forEach { (k, v) -> builder.appendLine("  $k: $v") }
        }
        builder.appendLine("Body:")
        val bodyToUse = prettyResponseBody ?: responseBody
        if (bodyToUse.isNullOrBlank()) {
            builder.appendLine("  (Empty)")
        } else {
            builder.appendLine(bodyToUse.prependIndent("  "))
        }
        return builder.toString().trimEnd()
    }
}
