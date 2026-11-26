package com.ugurbuga.inspectodemo

import com.ugurbuga.inspecto.InspectoURLSessionDataDelegate
import com.ugurbuga.inspecto.dataTaskWithRequestAndLog
import kotlinx.cinterop.BetaInteropApi
import platform.Foundation.*
import platform.darwin.NSObject

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

private class InspectoDemoSessionDataDelegate(
    private val onResult: (String) -> Unit
) : NSObject(), NSURLSessionDataDelegateProtocol {
    private var statusCode: Int = 0
    private var receivedLength: Long = 0

    override fun URLSession(
        session: NSURLSession,
        dataTask: NSURLSessionDataTask,
        didReceiveResponse: NSURLResponse,
        completionHandler: (NSURLSessionResponseDisposition) -> Unit
    ) {
        statusCode = (didReceiveResponse as? NSHTTPURLResponse)?.statusCode?.toInt() ?: 0
        completionHandler(NSURLSessionResponseAllow)
    }

    override fun URLSession(
        session: NSURLSession,
        dataTask: NSURLSessionDataTask,
        didReceiveData: NSData
    ) {
        receivedLength += didReceiveData.length.toLong()
    }

    override fun URLSession(
        session: NSURLSession,
        task: NSURLSessionTask,
        didCompleteWithError: NSError?
    ) {
        val resultMsg = if (didCompleteWithError != null) {
            "URLSession Delegate Error: ${didCompleteWithError.localizedDescription}"
        } else {
            "URLSession Delegate Response ($statusCode): Received $receivedLength bytes"
        }
        onResult(resultMsg)
    }
}

@OptIn(BetaInteropApi::class)
actual fun performNSURLSessionDelegateTestRequest(
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

    val appDelegate = InspectoDemoSessionDataDelegate(onResult)
    val inspectoDelegate = InspectoURLSessionDataDelegate(delegate = appDelegate)
    val session = NSURLSession.sessionWithConfiguration(
        configuration = NSURLSessionConfiguration.defaultSessionConfiguration,
        delegate = inspectoDelegate,
        delegateQueue = null
    )

    val task = session.dataTaskWithRequest(request)
    task.resume()
}
