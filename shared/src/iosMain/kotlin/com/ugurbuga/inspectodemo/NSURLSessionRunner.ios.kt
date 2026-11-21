package com.ugurbuga.inspectodemo

import com.ugurbuga.inspecto.logNSURLSessionDataTask
import platform.Foundation.*

actual val isNSURLSessionSupported: Boolean = true

actual fun performNSURLSessionTestRequest(url: String, onResult: (String) -> Unit) {
    val nsUrl = NSURL.URLWithString(url) ?: return onResult("Invalid URL")
    val request = NSMutableURLRequest.requestWithURL(nsUrl)
    val startTime = NSDate().timeIntervalSince1970 * 1000

    val task = NSURLSession.sharedSession.dataTaskWithRequest(request) { data, response, error ->
        val duration = ((NSDate().timeIntervalSince1970 * 1000) - startTime).toLong()
        val httpResponse = response as? NSHTTPURLResponse

        logNSURLSessionDataTask(
            request = request,
            response = httpResponse,
            data = data,
            error = error,
            durationMs = duration
        )

        val resultMsg = if (error != null) {
            "Error: ${error.localizedDescription}"
        } else {
            val length = data?.length ?: 0
            "Success (${httpResponse?.statusCode}): Received $length bytes via native NSURLSession"
        }
        onResult(resultMsg)
    }
    task.resume()
}
