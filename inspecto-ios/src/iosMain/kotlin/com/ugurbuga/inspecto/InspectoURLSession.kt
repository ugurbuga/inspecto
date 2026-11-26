package com.ugurbuga.inspecto

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.*
import platform.darwin.NSObject

private const val DEFAULT_HTTP_METHOD = "GET"
private const val DEFAULT_ERROR_STATUS_CODE = 0
private const val DEFAULT_SUCCESS_STATUS_CODE = 200

/**
 * Extension functions and helpers for automatic NSURLSession logging in Inspecto.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
fun NSURLSession.dataTaskWithRequestAndLog(
    request: NSURLRequest,
    onComplete: (NSData?, NSURLResponse?, NSError?) -> Unit
): NSURLSessionDataTask {
    val startTime = NSDate().timeIntervalSince1970
    val url = request.URL?.absoluteString.orEmpty()
    val method = request.HTTPMethod ?: DEFAULT_HTTP_METHOD

    val reqHeaders = (request.allHTTPHeaderFields as? Map<*, *>)?.entries?.associate {
        it.key.toString() to it.value.toString()
    } ?: emptyMap()

    val reqBodyString = request.HTTPBody?.let { data ->
        NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
    }.orEmpty()

    return dataTaskWithRequest(request) { data: NSData?, response: NSURLResponse?, error: NSError? ->
        val duration = ((NSDate().timeIntervalSince1970 - startTime) * 1000).toLong()
        val httpResponse = response as? NSHTTPURLResponse

        val statusCode = httpResponse?.statusCode?.toInt() ?: if (error != null) DEFAULT_ERROR_STATUS_CODE else DEFAULT_SUCCESS_STATUS_CODE

        val resHeaders = (httpResponse?.allHeaderFields as? Map<*, *>)?.entries?.associate {
            it.key.toString() to it.value.toString()
        } ?: emptyMap()

        val resBodyString = data?.let { d ->
            NSString.create(data = d, encoding = NSUTF8StringEncoding)?.toString()
        }.orEmpty().ifEmpty { if (error != null) "Error: ${error.localizedDescription}" else "" }

        Inspecto.addLog(
            url = url,
            method = method,
            statusCode = statusCode,
            durationMs = duration,
            requestHeaders = reqHeaders,
            requestBody = reqBodyString,
            responseHeaders = resHeaders,
            responseBody = resBodyString,
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
        val url = request?.URL?.absoluteString.orEmpty()
        val method = request?.HTTPMethod ?: DEFAULT_HTTP_METHOD

        val reqHeaders = (request?.allHTTPHeaderFields as? Map<*, *>)?.entries?.associate {
            it.key.toString() to it.value.toString()
        } ?: emptyMap()

        val resHeaders = (httpResponse?.allHeaderFields as? Map<*, *>)?.entries?.associate {
            it.key.toString() to it.value.toString()
        } ?: emptyMap()

        val reqBodyString = request?.HTTPBody?.let { data ->
            NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
        }.orEmpty()

        val statusCode = httpResponse?.statusCode?.toInt() ?: if (didCompleteWithError != null) DEFAULT_ERROR_STATUS_CODE else DEFAULT_SUCCESS_STATUS_CODE

        val resBodyString = responseData?.let { data ->
            NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
        }.orEmpty().ifEmpty { if (didCompleteWithError != null) "Error: ${didCompleteWithError.localizedDescription}" else "" }

        Inspecto.addLog(
            url = url,
            method = method,
            statusCode = statusCode,
            durationMs = duration,
            requestHeaders = reqHeaders,
            requestBody = reqBodyString,
            responseHeaders = resHeaders,
            responseBody = resBodyString,
            tag = InspectoTag.NSURLSESSION
        )

        delegate?.URLSession(session, task, didCompleteWithError)
    }
}
