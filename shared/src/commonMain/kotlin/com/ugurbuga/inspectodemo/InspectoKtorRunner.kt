package io.github.ugurbuga.inspectodemo

import io.github.ugurbuga.inspecto.Inspecto
import io.github.ugurbuga.inspecto.InspectoKtorLogging
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object InspectoKtorRunner {
    val ktorClient: HttpClient by lazy {
        HttpClient {
            install(InspectoKtorLogging)
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                })
            }
        }
    }

    suspend fun fetchUsers(): String {
        val response: HttpResponse = ktorClient.get("https://jsonplaceholder.typicode.com/users")
        val body = response.bodyAsText()
        return "Success (${response.status.value}): Received ${body.length} chars."
    }

    suspend fun fetchPosts(): String {
        val response: HttpResponse = ktorClient.get("https://jsonplaceholder.typicode.com/posts")
        val body = response.bodyAsText()
        return "Success (${response.status.value}): Received ${body.length} chars."
    }

    suspend fun submitPost(): String {
        val response: HttpResponse = ktorClient.post("https://jsonplaceholder.typicode.com/posts") {
            contentType(ContentType.Application.Json)
            setBody("{\"title\": \"Inspecto Test Post\", \"body\": \"Hello from Ktor POST\", \"userId\": 1}")
        }
        val body = response.bodyAsText()
        return "Success (${response.status.value}): Received ${body.length} chars."
    }

    suspend fun fetchComments(): String {
        val response: HttpResponse = ktorClient.get("https://jsonplaceholder.typicode.com/comments")
        val body = response.bodyAsText()
        return "Success (${response.status.value}): Received ${body.length} chars."
    }

    suspend fun triggerBadRequest400(): String {
        return try {
            val response: HttpResponse = ktorClient.get("https://httpstat.us/400")
            "HTTP 400 Bad Request (${response.status.value})"
        } catch (e: Exception) {
            Inspecto.addLog(
                url = "https://api.example.com/v1/checkout/apply-coupon",
                method = "POST",
                statusCode = 400,
                durationMs = 312,
                requestHeaders = mapOf("Content-Type" to "application/json", "Authorization" to "Bearer sample_token_123"),
                requestBody = "{\n  \"cartId\": \"cart_99812\",\n  \"couponCode\": \"EXPIRED_COUPON_2024\"\n}",
                responseHeaders = mapOf("Content-Type" to "application/json; charset=utf-8"),
                responseBody = "{\n  \"status\": 400,\n  \"error\": \"BAD_REQUEST\",\n  \"message\": \"Coupon code 'EXPIRED_COUPON_2024' is invalid or expired.\"\n}",
                tag = "Ktor"
            )
            "Triggered 400 Bad Request Error"
        }
    }

    suspend fun triggerServerError500(): String {
        return try {
            val response: HttpResponse = ktorClient.get("https://httpstat.us/500")
            "HTTP 500 Internal Server Error (${response.status.value})"
        } catch (e: Exception) {
            Inspecto.addLog(
                url = "https://api.example.com/v1/payment/process-charge",
                method = "POST",
                statusCode = 500,
                durationMs = 1240,
                requestHeaders = mapOf("Content-Type" to "application/json", "Authorization" to "Bearer sample_token_123"),
                requestBody = "{\n  \"amount\": 149.99,\n  \"currency\": \"USD\",\n  \"paymentMethodId\": \"pm_card_visa\"\n}",
                responseHeaders = mapOf("Content-Type" to "application/json; charset=utf-8"),
                responseBody = "{\n  \"status\": 500,\n  \"error\": \"INTERNAL_SERVER_ERROR\",\n  \"message\": \"Database transaction timeout in payment gateway service.\"\n}",
                tag = "Ktor"
            )
            "Triggered 500 Internal Server Error"
        }
    }
}
