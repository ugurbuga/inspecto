package com.ugurbuga.inspectodemo

import com.ugurbuga.inspecto.InspectoOkHttpLoggingInterceptor
import okhttp3.OkHttpClient
import okhttp3.Request

private val client = OkHttpClient.Builder()
    .addInterceptor(InspectoOkHttpLoggingInterceptor())
    .build()

actual fun performOkHttpTestRequest(url: String, onResult: (String) -> Unit) {
    Thread {
        try {
            val request = Request.Builder().url(url).header("User-Agent", "InspectoDemo-OkHttp").build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            onResult("Success (${response.code}): Received ${body.length} chars")
        } catch (e: Exception) {
            onResult("Error: ${e.message}")
        }
    }.start()
}

actual val isOkHttpSupported: Boolean = true
