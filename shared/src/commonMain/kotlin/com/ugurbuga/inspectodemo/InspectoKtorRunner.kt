package com.ugurbuga.inspectodemo

import com.ugurbuga.inspecto.InspectoKtorLogging
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
        val response: HttpResponse = ktorClient.post("https://reqres.in/api/login") {
            contentType(ContentType.Application.Json)
            setBody("{}")
        }
        return "HTTP 400 Bad Request (${response.status.value})"
    }

    suspend fun triggerServerError500(): String {
        val response: HttpResponse = ktorClient.get("https://dummyjson.com/http/500")
        return "HTTP 500 Internal Server Error (${response.status.value})"
    }
}
