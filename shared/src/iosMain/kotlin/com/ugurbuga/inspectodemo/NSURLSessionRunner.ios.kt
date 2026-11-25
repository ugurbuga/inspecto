package com.ugurbuga.inspectodemo

import com.ugurbuga.inspecto.dataTaskWithRequestAndLog
import kotlinx.cinterop.BetaInteropApi
import platform.Foundation.*

actual val isNSURLSessionSupported: Boolean = true

private val inspectoSession: NSURLSession by lazy {
    val config = NSURLSessionConfiguration.defaultSessionConfiguration
    NSURLSession.sessionWithConfiguration(config)
}

@OptIn(BetaInteropApi::class)
actual fun performNSURLSessionTestRequest(
    url: String,
    method: String,
    body: String?,
    onResult: (String) -> Unit
) {
    val nsUrl = NSURL.URLWithString(url) ?: return onResult("Invalid URL")
    val request = NSMutableURLRequest.requestWithURL(nsUrl)
    request.HTTPMethod = method
    if (body != null) {
        request.setValue("application/json", forHTTPHeaderField = "Content-Type")
        request.HTTPBody = NSString.create(string = body).dataUsingEncoding(NSUTF8StringEncoding)
    }

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
