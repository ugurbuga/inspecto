package io.github.ugurbuga.inspecto

/**
 * Helper object for logging GraphQL queries, mutations, and subscriptions in Inspecto.
 *
 * Works with Apollo Kotlin, Ktor GraphQL, and any custom GraphQL client.
 *
 * Usage:
 * ```kotlin
 * InspectoGraphQL.logOperation(
 *     operationName = "GetUserProfile",
 *     operationType = "QUERY",
 *     endpointUrl = "https://api.example.com/graphql",
 *     queryVariables = "{\"userId\": 42}",
 *     responseDataBody = "{\"data\": {\"user\": {\"name\": \"John Doe\"}}}",
 *     durationMs = 120L
 * )
 * ```
 */
object InspectoGraphQL {

    /**
     * Logs a GraphQL operation (query, mutation, subscription) in Inspecto.
     */
    fun logOperation(
        operationName: String,
        operationType: String = "QUERY",
        endpointUrl: String = "https://api.graphql.local/graphql",
        queryVariables: String? = null,
        responseDataBody: String? = null,
        statusCode: Int = 200,
        durationMs: Long = 0L,
        isError: Boolean = false
    ) {
        val rawRequestBody = buildString {
            append("{\"operationName\":\"$operationName\",\"type\":\"$operationType\"")
            if (!queryVariables.isNullOrBlank()) {
                append(",\"variables\":$queryVariables")
            }
            append("}")
        }

        val formattedRequestBody = Inspecto.formatJson(rawRequestBody)
        val formattedResponseBody = Inspecto.formatJson(responseDataBody ?: "{}")

        Inspecto.addLog(
            url = if (endpointUrl.contains("#")) endpointUrl else "$endpointUrl#$operationName",
            method = operationType.uppercase(),
            statusCode = if (isError && statusCode == 200) 500 else statusCode,
            durationMs = durationMs,
            requestHeaders = mapOf(
                "Content-Type" to "application/json",
                "X-GraphQL-Operation" to operationName
            ),
            requestBody = formattedRequestBody,
            responseHeaders = mapOf("Content-Type" to "application/json"),
            responseBody = formattedResponseBody,
            tag = InspectoTag.GRAPHQL
        )
    }
}
