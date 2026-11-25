package com.ugurbuga.inspectodemo

import com.ugurbuga.inspecto.dataTaskWithRequestAndLog
import platform.Foundation.*

actual val isNSURLSessionSupported: Boolean = true

private val inspectoSession: NSURLSession by lazy {
    val config = NSURLSessionConfiguration.defaultSessionConfiguration
    NSURLSession.sessionWithConfiguration(config)
}

private var requestToggle = false

actual fun performNSURLSessionTestRequest(url: String, onResult: (String) -> Unit) {
    requestToggle = !requestToggle
    // Alternate between a successful call (200 OK) and a failed call (404 Not Found)
    val targetUrl = if (requestToggle) {
        "https://httpbin.org/get"
    } else {
        "https://httpbin.org/status/404"
    }

    val nsUrl = NSURL.URLWithString(targetUrl) ?: return onResult("Invalid URL")
    val request = NSMutableURLRequest.requestWithURL(nsUrl)
    request.HTTPMethod = "GET"

    // 100% automated logging via Inspecto extension function!
    val task = inspectoSession.dataTaskWithRequestAndLog(request) { data, response, error ->
        val httpResponse = response as? NSHTTPURLResponse
        val statusCode = httpResponse?.statusCode?.toInt() ?: 0

        val resultMsg = if (error != null) {
            "URLSession Error: ${error.localizedDescription}"
        } else {
            val length = data?.length ?: 0
            "URLSession Response ($statusCode): Received $length bytes (Logged automatically by Inspecto)"
        }
        onResult(resultMsg)
    }
    task.resume()
}
