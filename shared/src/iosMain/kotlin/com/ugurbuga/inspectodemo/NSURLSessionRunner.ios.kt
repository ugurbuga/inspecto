package com.ugurbuga.inspectodemo

import com.ugurbuga.inspecto.dataTaskWithRequestAndLog
import platform.Foundation.*

actual val isNSURLSessionSupported: Boolean = true

private val inspectoSession: NSURLSession by lazy {
    val config = NSURLSessionConfiguration.defaultSessionConfiguration
    NSURLSession.sessionWithConfiguration(config)
}

actual fun performNSURLSessionTestRequest(url: String, onResult: (String) -> Unit) {
    val nsUrl = NSURL.URLWithString(url) ?: return onResult("Invalid URL")
    val request = NSMutableURLRequest.requestWithURL(nsUrl)
    request.HTTPMethod = "GET"

    val task = inspectoSession.dataTaskWithRequestAndLog(request) { data, response, error ->
        val httpResponse = response as? NSHTTPURLResponse
        val statusCode = httpResponse?.statusCode?.toInt() ?: 0

        val resultMsg = if (error != null) {
            "URLSession Error: ${error.localizedDescription}"
        } else {
            val length = data?.length ?: 0
            "URLSession Response ($statusCode): Received $length bytes"
        }
        onResult(resultMsg)
    }
    task.resume()
}
