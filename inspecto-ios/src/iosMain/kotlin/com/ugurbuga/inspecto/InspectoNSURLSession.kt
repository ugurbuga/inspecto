package com.ugurbuga.inspecto

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSString
import platform.Foundation.NSURLRequest
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create

/**
 * Native iOS NSURLSession logging helper for Inspecto.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
fun logNSURLSessionCall(
    url: String,
    method: String = "GET",
    statusCode: Int = 200,
    durationMs: Long = 0L,
    requestHeaders: Map<String, String> = emptyMap(),
    responseHeaders: Map<String, String> = emptyMap(),
    requestBody: String? = null,
    responseBody: String? = null,
    error: NSError? = null
) {
    Inspecto.addLog(
        url = url,
        method = method,
        statusCode = if (error != null) 0 else statusCode,
        durationMs = durationMs,
        requestHeaders = requestHeaders,
        responseHeaders = responseHeaders,
        requestBody = requestBody,
        responseBody = if (error != null) "Error: ${error.localizedDescription}" else (responseBody ?: ""),
        tag = "iOS NSURLSession"
    )
}

@OptIn(BetaInteropApi::class)
fun logNSURLSessionDataTask(
    request: NSURLRequest,
    response: NSHTTPURLResponse? = null,
    data: NSData? = null,
    error: NSError? = null,
    durationMs: Long = 0L
) {
    val url = request.URL?.absoluteString ?: ""
    val bodyString = if (data != null) {
        NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString() ?: ""
    } else ""

    val resHeaders = (response?.allHeaderFields as? Map<*, *>)
        ?.entries
        ?.associate { it.key.toString() to it.value.toString() } ?: emptyMap()

    logNSURLSessionCall(
        url = url,
        method = "HTTP",
        statusCode = response?.statusCode?.toInt() ?: if (error != null) 0 else 200,
        durationMs = durationMs,
        responseHeaders = resHeaders,
        responseBody = bodyString,
        error = error
    )
}
