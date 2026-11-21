package io.github.ugurbuga.inspecto

import com.apollographql.apollo.api.ApolloRequest
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.interceptor.ApolloInterceptor
import com.apollographql.apollo.interceptor.ApolloInterceptorChain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Apollo Kotlin Interceptor for Inspecto.
 *
 * Automatically intercepts and logs all Apollo GraphQL requests and responses across Android, iOS, Desktop, and Web.
 * Requires the user to explicitly provide the [endpointUrl] in the constructor (no static defaults inside the library).
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
        val operationType = when {
            document.contains("mutation") -> "MUTATION"
            document.contains("subscription") -> "SUBSCRIPTION"
            else -> "QUERY"
        }

        return chain.proceed(request)
            .onEach { response ->
                val duration = kotlin.time.Clock.System.now().toEpochMilliseconds() - startTime
                val hasErrors = !response.errors.isNullOrEmpty()
                val statusCode = if (hasErrors) 500 else 200

                val dataJson = response.data?.let { dataObj ->
                    formatApolloDataToJson(dataObj)
                } ?: "null"

                val rawResponseBody = buildString {
                    append("{\"data\":")
                    append(dataJson)
                    if (hasErrors) {
                        append(",\"errors\":${response.errors}}")
                    } else {
                        append("}")
                    }
                }

                val formattedBody = Inspecto.formatJson(rawResponseBody)

                InspectoGraphQL.logOperation(
                    operationName = operationName,
                    operationType = operationType,
                    endpointUrl = endpointUrl,
                    queryVariables = null,
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
                    queryVariables = null,
                    responseDataBody = Inspecto.formatJson("{\"error\": \"${throwable.message}\"}"),
                    statusCode = 500,
                    durationMs = duration,
                    isError = true
                )
                throw throwable
            }
    }

    private fun formatApolloDataToJson(value: Any?): String {
        if (value == null) return "null"
        return runCatching {
            val jsonElement = toJsonElement(value)
            jsonElement.toString()
        }.getOrDefault("null")
    }

    private fun toJsonElement(value: Any?): JsonElement {
        return when (value) {
            null -> JsonNull
            is Map<*, *> -> {
                val map = mutableMapOf<String, JsonElement>()
                for ((k, v) in value) {
                    if (k != null) {
                        map[k.toString()] = toJsonElement(v)
                    }
                }
                JsonObject(map)
            }
            is List<*> -> {
                JsonArray(value.map { toJsonElement(it) })
            }
            is String -> JsonPrimitive(value)
            is Number -> JsonPrimitive(value)
            is Boolean -> JsonPrimitive(value)
            else -> {
                val str = value.toString()
                if (str.startsWith("Data(") && str.endsWith(")")) {
                    parseKotlinDataClassToJson(str)
                } else {
                    JsonPrimitive(str)
                }
            }
        }
    }

    private fun parseKotlinDataClassToJson(str: String): JsonElement {
        return runCatching {
            var s = str.trim()
            if (s.startsWith("Data(") && s.endsWith(")")) {
                s = s.substring(5, s.length - 1)
            }
            if (s.startsWith("characters=")) {
                val inner = s.substring("characters=".length)
                JsonObject(mapOf("characters" to parseKotlinMapStringToJson(inner)))
            } else {
                JsonPrimitive(s)
            }
        }.getOrDefault(JsonNull)
    }

    private fun parseKotlinMapStringToJson(str: String): JsonElement {
        return runCatching {
            val s = str.trim()
            when {
                s.startsWith("{") && s.endsWith("}") -> {
                    val inner = s.substring(1, s.length - 1)
                    val map = mutableMapOf<String, JsonElement>()
                    var i = 0
                    while (i < inner.length) {
                        val equalsIdx = inner.indexOf('=', i)
                        if (equalsIdx == -1) break
                        val key = inner.substring(i, equalsIdx).trim().trim('{', ',', ' ')

                        var depth = 0
                        var endIdx = equalsIdx + 1
                        while (endIdx < inner.length) {
                            val c = inner[endIdx]
                            if (c == '{' || c == '[') depth++
                            if (c == '}' || c == ']') depth--
                            if (c == ',' && depth == 0) break
                            endIdx++
                        }

                        val valStr = inner.substring(equalsIdx + 1, endIdx).trim()
                        map[key] = parseKotlinMapStringToJson(valStr)
                        i = endIdx + 1
                    }
                    JsonObject(map)
                }
                s.startsWith("[") && s.endsWith("]") -> {
                    val inner = s.substring(1, s.length - 1)
                    val list = mutableListOf<JsonElement>()
                    var depth = 0
                    var start = 0
                    for (i in inner.indices) {
                        val c = inner[i]
                        if (c == '{' || c == '[') depth++
                        if (c == '}' || c == ']') depth--
                        if (c == ',' && depth == 0) {
                            list.add(parseKotlinMapStringToJson(inner.substring(start, i).trim()))
                            start = i + 1
                        }
                    }
                    if (start < inner.length) {
                        list.add(parseKotlinMapStringToJson(inner.substring(start).trim()))
                    }
                    JsonArray(list)
                }
                else -> {
                    when {
                        s == "true" -> JsonPrimitive(true)
                        s == "false" -> JsonPrimitive(false)
                        s == "null" -> JsonNull
                        s.toDoubleOrNull() != null -> JsonPrimitive(s.toDouble())
                        else -> JsonPrimitive(s)
                    }
                }
            }
        }.getOrDefault(JsonPrimitive(str))
    }
}
