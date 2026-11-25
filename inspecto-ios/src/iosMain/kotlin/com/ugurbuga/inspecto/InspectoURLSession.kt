package com.ugurbuga.inspecto

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.*
import platform.darwin.NSObject

/**
 * Extension functions and helpers for automatic NSURLSession logging in Inspecto.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
fun NSURLSession.dataTaskWithRequestAndLog(
    request: NSURLRequest,
    onComplete: (NSData?, NSURLResponse?, NSError?) -> Unit
): NSURLSessionDataTask {
    val startTime = NSDate().timeIntervalSince1970
    val url = request.URL?.absoluteString ?: ""
    val method = request.HTTPMethod ?: "GET"

    val reqHeaders = (request.allHTTPHeaderFields as? Map<*, *>)?.entries?.associate {
        it.key.toString() to it.value.toString()
    } ?: emptyMap()

    val reqBodyString = request.HTTPBody?.let { data ->
        NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
    }

    return dataTaskWithRequest(request) { data, response, error ->
        val duration = ((NSDate().timeIntervalSince1970 - startTime) * 1000).toLong()
        val httpResponse = response as? NSHTTPURLResponse
        val statusCode = httpResponse?.statusCode?.toInt() ?: if (error != null) 0 else 200

        val resHeaders = (httpResponse?.allHeaderFields as? Map<*, *>)?.entries?.associate {
            it.key.toString() to it.value.toString()
        } ?: emptyMap()

        val resBodyString = data?.let { d ->
            NSString.create(data = d, encoding = NSUTF8StringEncoding)?.toString()
        } ?: ""

        Inspecto.addLog(
            url = url,
            method = method,
            statusCode = statusCode,
            durationMs = duration,
            requestHeaders = reqHeaders,
            requestBody = reqBodyString,
            responseHeaders = resHeaders,
            responseBody = if (error != null) "Error: ${error.localizedDescription}" else resBodyString,
            tag = InspectoTag.NSURLSESSION
        )

        onComplete(data, response, error)
    }
}

/**
 * Delegate wrapper for NSURLSession that automatically captures and logs network calls to Inspecto.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class InspectoURLSessionDataDelegate(
    private val delegate: NSURLSessionDataDelegateProtocol? = null
) : NSObject(), NSURLSessionDataDelegateProtocol {

    private var startTime: Double = 0.0
    private var responseData: NSMutableData? = null
    private var httpResponse: NSHTTPURLResponse? = null

    override fun URLSession(
        session: NSURLSession,
        dataTask: NSURLSessionDataTask,
        didReceiveResponse: NSURLResponse,
        completionHandler: (NSURLSessionResponseDisposition) -> Unit
    ) {
        httpResponse = didReceiveResponse as? NSHTTPURLResponse
        responseData = NSMutableData()
        startTime = NSDate().timeIntervalSince1970
        delegate?.URLSession(session, dataTask, didReceiveResponse, completionHandler)
            ?: completionHandler(NSURLSessionResponseAllow)
    }

    override fun URLSession(
        session: NSURLSession,
        dataTask: NSURLSessionDataTask,
        didReceiveData: NSData
    ) {
        responseData?.appendData(didReceiveData)
        delegate?.URLSession(session, dataTask, didReceiveData)
    }

    override fun URLSession(
        session: NSURLSession,
        task: NSURLSessionTask,
        didCompleteWithError: NSError?
    ) {
        val duration = ((NSDate().timeIntervalSince1970 - startTime) * 1000).toLong()
        val request = task.originalRequest ?: task.currentRequest
        val url = request?.URL?.absoluteString ?: ""
        val method = request?.HTTPMethod ?: "GET"

        val reqHeaders = (request?.allHTTPHeaderFields as? Map<*, *>)?.entries?.associate {
            it.key.toString() to it.value.toString()
        } ?: emptyMap()

        val resHeaders = (httpResponse?.allHeaderFields as? Map<*, *>)?.entries?.associate {
            it.key.toString() to it.value.toString()
        } ?: emptyMap()

        val reqBodyString = request?.HTTPBody?.let { data ->
            NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
        }

        val resBodyString = responseData?.let { data ->
            NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
        } ?: ""

        Inspecto.addLog(
            url = url,
            method = method,
            statusCode = httpResponse?.statusCode?.toInt() ?: if (didCompleteWithError != null) 0 else 200,
            durationMs = duration,
            requestHeaders = reqHeaders,
            requestBody = reqBodyString,
            responseHeaders = resHeaders,
            responseBody = if (didCompleteWithError != null) "Error: ${didCompleteWithError.localizedDescription}" else resBodyString,
            tag = InspectoTag.NSURLSESSION
        )

        delegate?.URLSession(session, task, didCompleteWithError)
    }
}
