package com.ugurbuga.inspecto

import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer

class InspectoOkHttpLoggingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startTime = System.currentTimeMillis()

        val requestBodyString = request.body?.let { body ->
            val buffer = Buffer()
            body.writeTo(buffer)
            buffer.readUtf8()
        }

        val response = try {
            chain.proceed(request)
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            InspectoOkHttpInterceptor.logCall(
                url = request.url.toString(),
                method = request.method,
                statusCode = 0,
                durationMs = duration,
                requestHeaders = request.headers.toMap(),
                requestBody = requestBodyString,
                responseBody = "Error: ${e.message}",
                tag = "OkHttp"
            )
            throw e
        }

        val duration = System.currentTimeMillis() - startTime
        val responseBodyString = response.body?.string() ?: ""

        val contentType = response.body?.contentType()
        val body = okhttp3.ResponseBody.create(contentType, responseBodyString)
        val newResponse = response.newBuilder().body(body).build()

        InspectoOkHttpInterceptor.logCall(
            url = request.url.toString(),
            method = request.method,
            statusCode = response.code,
            durationMs = duration,
            requestHeaders = request.headers.toMap(),
            queryParams = request.url.queryParameterNames.associateWith { request.url.queryParameter(it) ?: "" },
            requestBody = requestBodyString,
            responseHeaders = response.headers.toMap(),
            responseBody = responseBodyString,
            tag = "OkHttp"
        )

        return newResponse
    }
}
