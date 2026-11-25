package com.ugurbuga.inspecto

import com.apollographql.apollo.api.ApolloRequest
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.interceptor.ApolloInterceptor
import com.apollographql.apollo.interceptor.ApolloInterceptorChain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach

/**
 * Apollo Kotlin Interceptor for Inspecto.
 *
 * Automatically intercepts and logs all Apollo GraphQL requests and responses across Android, iOS, Desktop, and Web.
 * Requires the user to explicitly provide the [endpointUrl] in the constructor.
 *
 * Usage:
 * ```kotlin
 * val url = "https://api.example.com/graphql"
 * val apolloClient = ApolloClient.Builder()
 *     .serverUrl(url)
 *     .addInterceptor(InspectoApolloInterceptor(url))
 *     .build()
 * ```
 */
class InspectoApolloInterceptor(
    private val endpointUrl: String
) : ApolloInterceptor {
    override fun <D : Operation.Data> intercept(
        request: ApolloRequest<D>,
        chain: ApolloInterceptorChain
    ): Flow<ApolloResponse<D>> {
        val startTime = kotlin.time.Clock.System.now().toEpochMilliseconds()
        val operationName = request.operation.name().ifBlank { "GraphQLOperation" }

        val document = try {
            request.operation.document().lowercase()
        } catch (_: Throwable) {
            "query"
        }
        val queryDocument = try {
            request.operation.document()
        } catch (_: Throwable) {
            null
        }
        val operationType = when {
            document.contains("mutation") -> "MUTATION"
            document.contains("subscription") -> "SUBSCRIPTION"
            else -> "QUERY"
        }

        val queryVariables = runCatching {
            val buffer = okio.Buffer()
            val writer = com.apollographql.apollo.api.json.BufferedSinkJsonWriter(buffer)
            writer.beginObject()
            request.operation.serializeVariables(writer, com.apollographql.apollo.api.CustomScalarAdapters.Empty, false)
            writer.endObject()
            writer.flush()
            val str = buffer.readUtf8()
            if (str == "{}") null else str
        }.getOrNull()

        return chain.proceed(request)
            .onEach { response ->
                val duration = kotlin.time.Clock.System.now().toEpochMilliseconds() - startTime
                val httpException = response.exception as? com.apollographql.apollo.exception.ApolloHttpException
                val hasErrors = !response.errors.isNullOrEmpty() || response.exception != null
                val statusCode = httpException?.statusCode ?: if (hasErrors) 500 else 200

                val rawResponseBody = when {
                    response.exception != null -> {
                        val errMessage = response.exception?.message ?: "Unknown error"
                        "{\"error\": \"${errMessage.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", "")}\"}"
                    }
                    !response.errors.isNullOrEmpty() -> {
                        "{\"data\":null,\"errors\":${response.errors?.map { mapOf("message" to it.message) }}}"
                    }
                    else -> {
                        response.data?.let { dataObj ->
                            formatApolloResponseData(request, dataObj)
                        } ?: "{\"data\":null}"
                    }
                }

                val formattedBody = Inspecto.formatJson(rawResponseBody)

                InspectoGraphQL.logOperation(
                    operationName = operationName,
                    operationType = operationType,
                    endpointUrl = endpointUrl,
                    queryDocument = queryDocument,
                    queryVariables = queryVariables,
                    responseDataBody = formattedBody,
                    statusCode = statusCode,
                    durationMs = duration,
                    isError = hasErrors
                )
            }
            .catch { throwable ->
                val duration = kotlin.time.Clock.System.now().toEpochMilliseconds() - startTime
                InspectoGraphQL.logOperation(
                    operationName = operationName,
                    operationType = operationType,
                    endpointUrl = endpointUrl,
                    queryDocument = queryDocument,
                    queryVariables = queryVariables,
                    responseDataBody = Inspecto.formatJson("{\"error\": \"${throwable.message}\"}"),
                    statusCode = 500,
                    durationMs = duration,
                    isError = true
                )
                throw throwable
            }
    }

    private fun <D : Operation.Data> formatApolloResponseData(request: ApolloRequest<D>, data: D): String {
        return runCatching {
            val buffer = okio.Buffer()
            val writer = com.apollographql.apollo.api.json.BufferedSinkJsonWriter(buffer)
            writer.beginObject()
            writer.name("data")
            request.operation.adapter().toJson(writer, com.apollographql.apollo.api.CustomScalarAdapters.Empty, data)
            writer.endObject()
            writer.flush()
            val result = buffer.readUtf8()
            if (result.isBlank()) anyToJson(mapOf("data" to data)) else result
        }.getOrElse {
            anyToJson(mapOf("data" to data))
        }
    }

    private fun anyToJson(value: Any?): String {
        if (value == null) return "null"
        return when (value) {
            is Number, is Boolean -> value.toString()
            is String -> "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r") + "\""
            is Map<*, *> -> {
                val entries = value.entries.mapNotNull { (k, v) ->
                    if (k != null) "\"$k\":${anyToJson(v)}" else null
                }
                "{" + entries.joinToString(",") + "}"
            }
            is Iterable<*> -> {
                val items = value.map { anyToJson(it) }
                "[" + items.joinToString(",") + "]"
            }
            is Array<*> -> {
                val items = value.map { anyToJson(it) }
                "[" + items.joinToString(",") + "]"
            }
            else -> {
                convertKotlinToStringToJson(value.toString())
            }
        }
    }

    private fun convertKotlinToStringToJson(input: String): String {
        val s = input.trim()
        if ((s.startsWith("{") && s.endsWith("}")) || (s.startsWith("(") && s.endsWith(")"))) {
            val content = s.substring(1, s.length - 1)
            return "{${convertContentToJson(content)}}"
        }
        val parenIdx = s.indexOf('(')
        if (parenIdx != -1 && s.endsWith(")")) {
            val content = s.substring(parenIdx + 1, s.length - 1)
            return "{${convertContentToJson(content)}}"
        }
        val braceIdx = s.indexOf('{')
        if (braceIdx != -1 && s.endsWith("}")) {
            val content = s.substring(braceIdx + 1, s.length - 1)
            return "{${convertContentToJson(content)}}"
        }
        return "\"" + s.replace("\"", "\\\"") + "\""
    }

    private fun convertContentToJson(content: String): String {
        val result = StringBuilder()
        var i = 0
        while (i < content.length) {
            val eqIdx = content.indexOf('=', i)
            if (eqIdx == -1) {
                result.append(content.substring(i))
                break
            }
            val key = content.substring(i, eqIdx).trim()
            result.append("\"$key\":")

            var depthParen = 0
            var depthBracket = 0
            var depthBrace = 0
            var j = eqIdx + 1
            while (j < content.length) {
                val c = content[j]
                when (c) {
                    '(' -> depthParen++
                    ')' -> depthParen--
                    '[' -> depthBracket++
                    ']' -> depthBracket--
                    '{' -> depthBrace++
                    '}' -> depthBrace--
                    ',' -> {
                        if (depthParen == 0 && depthBracket == 0 && depthBrace == 0) {
                            if (isNextFieldAssignment(content, j + 1)) break
                        }
                    }
                }
                j++
            }
            val rawVal = content.substring(eqIdx + 1, j).trim()
            result.append(parseValueToJson(rawVal))

            if (j < content.length && content[j] == ',') {
                result.append(",")
                i = j + 1
            } else {
                i = j
            }
        }
        return result.toString()
    }

    private fun isNextFieldAssignment(str: String, startIndex: Int): Boolean {
        var idx = startIndex
        while (idx < str.length && str[idx].isWhitespace()) {
            idx++
        }
        if (idx >= str.length) return false

        val startId = idx
        while (idx < str.length && (str[idx].isLetterOrDigit() || str[idx] == '_' || str[idx] == '$')) {
            idx++
        }
        if (idx == startId) return false

        while (idx < str.length && str[idx].isWhitespace()) {
            idx++
        }

        return idx < str.length && str[idx] == '='
    }

    private fun parseValueToJson(raw: String): String {
        val s = raw.trim()
        when {
            s == "null" || s.isEmpty() -> return "null"
            s == "true" || s == "false" -> return s
            s.toDoubleOrNull() != null -> return s
            s.startsWith("[") && s.endsWith("]") -> {
                val inner = s.substring(1, s.length - 1).trim()
                if (inner.isEmpty()) return "[]"
                val items = splitByComma(inner)
                return "[" + items.joinToString(",") { parseValueToJson(it) } + "]"
            }
            s.contains("=") && (s.contains("{") || s.contains("(")) -> {
                return convertKotlinToStringToJson(s)
            }
        }
        return "\"" + s.replace("\"", "\\\"") + "\""
    }

    private fun splitByComma(inner: String): List<String> {
        val list = mutableListOf<String>()
        var depthParen = 0
        var depthBracket = 0
        var depthBrace = 0
        var start = 0
        var k = 0
        while (k < inner.length) {
            val c = inner[k]
            when (c) {
                '(' -> depthParen++
                ')' -> depthParen--
                '[' -> depthBracket++
                ']' -> depthBracket--
                '{' -> depthBrace++
                '}' -> depthBrace--
                ',' -> {
                    if (depthParen == 0 && depthBracket == 0 && depthBrace == 0) {
                        list.add(inner.substring(start, k).trim())
                        start = k + 1
                    }
                }
            }
            k++
        }
        if (start < inner.length) {
            list.add(inner.substring(start).trim())
        }
        return list
    }
}
